package com.ordering.analytics.service;

import com.ordering.analytics.dto.AnalyticsSummaryDto;
import com.ordering.analytics.dto.OrderEventDto;
import com.ordering.analytics.model.ProductStats;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AggregationService {

    private final AtomicLong totalOrders = new AtomicLong(0);
    private final AtomicLong totalRetries = new AtomicLong(0);
    private final AtomicLong totalDlqMessages = new AtomicLong(0);
    private double totalRevenue = 0.0;
    private double minPrice = Double.MAX_VALUE;
    private double maxPrice = Double.MIN_VALUE;

    private final Map<String, ProductStats> productStatsMap = new ConcurrentHashMap<>();
    private final Deque<OrderEventDto> recentEvents = new ConcurrentLinkedDeque<>();
    private static final int MAX_RECENT_EVENTS = 50;

    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    public synchronized void recordValidOrder(String orderId, String product, double price, String topic) {
        this.totalOrders.incrementAndGet();
        this.totalRevenue += price;

        if (price < this.minPrice) {
            this.minPrice = price;
        }
        if (price > this.maxPrice) {
            this.maxPrice = price;
        }

        productStatsMap.computeIfAbsent(product, k -> new ProductStats()).recordOrder(price);

        OrderEventDto event = new OrderEventDto(
                orderId,
                product,
                price,
                "PROCESSED",
                topic,
                0,
                String.format("Successfully processed. Running avg: $%.2f", getRunningAveragePrice())
        );
        addEvent(event);
        broadcastUpdate();
    }

    public void recordRetry(String orderId, String product, double price, String topic, int retryCount, String reason) {
        totalRetries.incrementAndGet();
        OrderEventDto event = new OrderEventDto(
                orderId,
                product,
                price,
                "RETRYING",
                topic,
                retryCount,
                String.format("Temporary failure (attempt %d): %s", retryCount, reason)
        );
        addEvent(event);
        broadcastUpdate();
    }

    public void recordDlq(String orderId, String product, double price, String topic, int retryCount, String reason) {
        totalDlqMessages.incrementAndGet();
        OrderEventDto event = new OrderEventDto(
                orderId,
                product,
                price,
                "DLQ",
                topic,
                retryCount,
                String.format("Permanent failure routed to DLQ: %s", reason)
        );
        addEvent(event);
        broadcastUpdate();
    }

    private void addEvent(OrderEventDto event) {
        recentEvents.addFirst(event);
        while (recentEvents.size() > MAX_RECENT_EVENTS) {
            recentEvents.removeLast();
        }
    }

    public synchronized double getRunningAveragePrice() {
        long orders = totalOrders.get();
        if (orders == 0) return 0.0;
        return Math.round((totalRevenue / orders) * 100.0) / 100.0;
    }

    public synchronized AnalyticsSummaryDto getSummary() {
        AnalyticsSummaryDto summary = new AnalyticsSummaryDto();
        summary.setTotalOrders(totalOrders.get());
        summary.setTotalRevenue(Math.round(totalRevenue * 100.0) / 100.0);
        summary.setRunningAveragePrice(getRunningAveragePrice());
        summary.setMinPrice(totalOrders.get() == 0 ? 0.0 : Math.round(minPrice * 100.0) / 100.0);
        summary.setMaxPrice(totalOrders.get() == 0 ? 0.0 : Math.round(maxPrice * 100.0) / 100.0);
        summary.setTotalRetries(totalRetries.get());
        summary.setTotalDlqMessages(totalDlqMessages.get());
        summary.setLastUpdated(Instant.now().toString());

        Map<String, AnalyticsSummaryDto.ProductMetricsDto> prodDtoMap = new HashMap<>();
        for (Map.Entry<String, ProductStats> entry : productStatsMap.entrySet()) {
            ProductStats stats = entry.getValue();
            prodDtoMap.put(entry.getKey(), new AnalyticsSummaryDto.ProductMetricsDto(
                    stats.getCount(),
                    stats.getTotalRevenue(),
                    stats.getAveragePrice(),
                    stats.getMinPrice(),
                    stats.getMaxPrice()
            ));
        }
        summary.setPerProduct(prodDtoMap);
        return summary;
    }

    public List<OrderEventDto> getRecentEvents() {
        return new ArrayList<>(recentEvents);
    }

    public synchronized void reset() {
        totalOrders.set(0);
        totalRetries.set(0);
        totalDlqMessages.set(0);
        totalRevenue = 0.0;
        minPrice = Double.MAX_VALUE;
        maxPrice = Double.MIN_VALUE;
        productStatsMap.clear();
        recentEvents.clear();
        broadcastUpdate();
    }

    public SseEmitter registerSseEmitter() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 min timeout
        sseEmitters.add(emitter);

        emitter.onCompletion(() -> sseEmitters.remove(emitter));
        emitter.onTimeout(() -> sseEmitters.remove(emitter));
        emitter.onError(e -> sseEmitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("init").data(getSummary()));
        } catch (IOException e) {
            sseEmitters.remove(emitter);
        }

        return emitter;
    }

    private void broadcastUpdate() {
        AnalyticsSummaryDto summary = getSummary();
        for (SseEmitter emitter : sseEmitters) {
            try {
                emitter.send(SseEmitter.event().name("metrics").data(summary));
            } catch (Exception e) {
                sseEmitters.remove(emitter);
            }
        }
    }
}
