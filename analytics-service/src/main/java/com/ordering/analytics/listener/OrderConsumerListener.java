package com.ordering.analytics.listener;

import com.ordering.avro.Order;
import com.ordering.analytics.service.AggregationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumerListener {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumerListener.class);
    private final AggregationService aggregationService;

    public OrderConsumerListener(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    /**
     * Main Kafka Consumer listener with Spring Kafka Non-Blocking Retryable Topics.
     * Retries transient failures with exponential backoff (1s, 2s, 4s).
     * Excludes IllegalArgumentException (fatal errors) so they route immediately to DLQ.
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 8000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR,
            dltTopicSuffix = "-dlq",
            autoCreateTopics = "true",
            exclude = {IllegalArgumentException.class}
    )
    @KafkaListener(
            topics = "${app.kafka.topics.orders:orders}",
            groupId = "${spring.kafka.consumer.group-id:order-analytics-consumer-group}"
    )
    public void consumeOrder(
            ConsumerRecord<String, Order> record,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC, defaultValue = "orders") String topic
    ) {
        Order order = record.value();
        if (order == null) {
            log.warn("Received empty/null order record on topic: {}", topic);
            return;
        }

        log.info("Received Order: orderId={}, product={}, price={}, topic={}",
                order.getOrderId(), order.getProduct(), order.getPrice(), topic);

        // 1. Simulated Fatal Failure (Non-recoverable) -> Routes directly to DLQ
        if (order.getProduct().contains("FATAL_FAIL") || order.getPrice() < 0) {
            log.error("[Fatal Error] Invalid order payload for orderId={}: price={}, product={}",
                    order.getOrderId(), order.getPrice(), order.getProduct());
            throw new IllegalArgumentException("Fatal unrecoverable error: Invalid price ($" + order.getPrice() + ") or forbidden item: " + order.getProduct());
        }

        // 2. Simulated Transient Failure (Recoverable on retry)
        if (order.getProduct().contains("TEMP_FAIL")) {
            if (!topic.contains("-retry")) {
                // First arrival on main topic -> Simulate lock and trigger retry
                log.warn("[Transient Error] Simulating temporary inventory lock for orderId={}", order.getOrderId());
                aggregationService.recordRetry(
                        order.getOrderId(),
                        order.getProduct(),
                        (double) order.getPrice(),
                        topic,
                        1,
                        "Simulated temporary inventory lock (queued for retry)"
                );
                throw new IllegalStateException("Temporary lock on item: " + order.getProduct());
            } else {
                // Arrived via retry topic -> Recovered and successfully unlocked!
                log.info("[Transient Error Resolved] Order {} successfully unlocked on retry topic {}",
                        order.getOrderId(), topic);
            }
        }

        // 3. Valid Order -> Process & update real-time running average
        aggregationService.recordValidOrder(
                order.getOrderId(),
                order.getProduct(),
                (double) order.getPrice(),
                topic
        );

        log.info("[Aggregated] Total Orders: {}, Current Running Average Price: ${}",
                aggregationService.getSummary().getTotalOrders(),
                aggregationService.getRunningAveragePrice());
    }

    /**
     * Handler invoked when a record is routed to the Dead Letter Queue (DLQ).
     */
    @DltHandler
    public void handleDlt(
            ConsumerRecord<String, Order> record,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC, defaultValue = "orders-dlq") String topic
    ) {
        Order order = record.value();
        String orderId = order != null ? order.getOrderId() : record.key();
        String product = order != null ? order.getProduct() : "UNKNOWN";
        double price = order != null ? order.getPrice() : 0.0;

        log.error("[DLQ PROCESSED] Order {} routed to Dead Letter Queue (Topic: {}). Product: {}, Price: {}",
                orderId, topic, product, price);

        aggregationService.recordDlq(
                orderId,
                product,
                price,
                topic,
                order != null && order.getPrice() < 0 ? 0 : 3,
                "Permanent failure routed to DLQ: Non-recoverable validation failure or exhausted retries"
        );
    }
}
