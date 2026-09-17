package com.ordering.order.service;

import com.ordering.avro.Order;
import com.ordering.order.dto.CreateOrderRequest;
import com.ordering.order.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderProducerService {

    private static final Logger log = LoggerFactory.getLogger(OrderProducerService.class);
    private final KafkaTemplate<String, Order> kafkaTemplate;
    private final AtomicLong orderSequence = new AtomicLong(1000);
    private final Random random = new Random();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Value("${app.kafka.topics.orders:orders}")
    private String ordersTopic;

    private static final List<ProductItem> SAMPLE_PRODUCTS = List.of(
            new ProductItem("Laptop Pro 16", 1200.0f, 2500.0f),
            new ProductItem("Wireless Headphones", 150.0f, 399.0f),
            new ProductItem("Ultra-Wide 4K Monitor", 300.0f, 850.0f),
            new ProductItem("Mechanical Gaming Keyboard", 75.0f, 180.0f),
            new ProductItem("Ergonomic Wireless Mouse", 40.0f, 120.0f),
            new ProductItem("Smart Fitness Watch", 180.0f, 450.0f),
            new ProductItem("USB-C Multiport Dock", 50.0f, 150.0f),
            new ProductItem("Portable SSD 1TB", 90.0f, 200.0f)
    );

    public OrderProducerService(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderResponse sendOrder(CreateOrderRequest request) {
        Order avroOrder = Order.newBuilder()
                .setOrderId(request.getOrderId())
                .setProduct(request.getProduct())
                .setPrice(request.getPrice())
                .build();

        log.info("Publishing Avro Order to Kafka topic '{}': orderId={}, product={}, price={}",
                ordersTopic, avroOrder.getOrderId(), avroOrder.getProduct(), avroOrder.getPrice());

        CompletableFuture<SendResult<String, Order>> future = kafkaTemplate.send(ordersTopic, avroOrder.getOrderId(), avroOrder);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Delivered order [{}] to partition [{}] at offset [{}]",
                        avroOrder.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to produce order [{}] to Kafka: {}", avroOrder.getOrderId(), ex.getMessage());
            }
        });

        return new OrderResponse(
                avroOrder.getOrderId(),
                avroOrder.getProduct(),
                avroOrder.getPrice(),
                "PUBLISHED",
                ordersTopic
        );
    }

    public Order generateRandomOrder(String mode) {
        String orderId = String.valueOf(orderSequence.incrementAndGet());
        String product;
        float price;

        if ("transient".equalsIgnoreCase(mode)) {
            product = "TEMP_FAIL-ServiceLock-Item" + random.nextInt(100);
            price = Math.round((50.0f + random.nextFloat() * 150.0f) * 100.0f) / 100.0f;
        } else if ("fatal".equalsIgnoreCase(mode)) {
            product = "FATAL_FAIL-InvalidItem";
            price = -99.99f; // Invalid negative price
        } else {
            ProductItem item = SAMPLE_PRODUCTS.get(random.nextInt(SAMPLE_PRODUCTS.size()));
            product = item.name();
            price = Math.round((item.minPrice() + random.nextFloat() * (item.maxPrice() - item.minPrice())) * 100.0f) / 100.0f;
        }

        return Order.newBuilder()
                .setOrderId(orderId)
                .setProduct(product)
                .setPrice(price)
                .build();
    }

    public void startBatchGeneration(int count, long delayMs, String mode) {
        executor.submit(() -> {
            log.info("Starting background batch order generation: count={}, delayMs={}, mode={}", count, delayMs, mode);
            for (int i = 0; i < count; i++) {
                String currentMode = mode;
                if ("mixed".equalsIgnoreCase(mode)) {
                    double r = random.nextDouble();
                    if (r < 0.70) currentMode = "normal";
                    else if (r < 0.90) currentMode = "transient";
                    else currentMode = "fatal";
                }

                Order order = generateRandomOrder(currentMode);
                CreateOrderRequest req = new CreateOrderRequest(order.getOrderId(), order.getProduct(), order.getPrice());
                sendOrder(req);

                if (delayMs > 0 && i < count - 1) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            log.info("Completed background batch order generation.");
        });
    }

    private record ProductItem(String name, float minPrice, float maxPrice) {}
}
