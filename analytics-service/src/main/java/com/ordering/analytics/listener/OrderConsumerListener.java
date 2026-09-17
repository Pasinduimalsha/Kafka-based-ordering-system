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
     * Retries transient failures up to 3 times with exponential backoff (1s, 2s, 4s).
     * Routes exhausted retries or fatal errors to orders-dlq topic automatically.
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 8000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR,
            dltTopicSuffix = "-dlq",
            autoCreateTopics = "true"
    )
    @KafkaListener(
            topics = "${app.kafka.topics.orders:orders}",
            groupId = "${spring.kafka.consumer.group-id:order-analytics-consumer-group}"
    )
    public void consumeOrder(
            ConsumerRecord<String, Order> record,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC, defaultValue = "orders") String topic,
            @Header(value = "retry_count", required = false, defaultValue = "0") String retryHeader
    ) {
        Order order = record.value();
        if (order == null) {
            log.warn("Received empty/null order record on topic: {}", topic);
            return;
        }

        int currentAttempt = 1;
        if (topic.contains("-retry")) {
            try {
                String suffix = topic.substring(topic.lastIndexOf("-retry-") + 7);
                currentAttempt = Integer.parseInt(suffix) + 1;
            } catch (Exception ignored) {
                currentAttempt = 2;
            }
        }

        log.info("Received Order: orderId={}, product={}, price={}, topic={}, attempt={}",
                order.getOrderId(), order.getProduct(), order.getPrice(), topic, currentAttempt);

        // Simulated Fatal Failure (Non-recoverable) -> Routes directly to DLQ
        if (order.getProduct().contains("FATAL_FAIL") || order.getPrice() < 0) {
            log.error("[Fatal Error] Invalid order payload for orderId={}: price={}, product={}",
                    order.getOrderId(), order.getPrice(), order.getProduct());
            throw new IllegalArgumentException("Fatal unrecoverable error: Invalid price or fatal test payload: " + order.getProduct());
        }

        // Simulated Transient Failure (Recoverable on final retry)
        if (order.getProduct().contains("TEMP_FAIL")) {
            if (currentAttempt < 3) {
                log.warn("[Transient Error] Simulating temporary inventory lock for orderId={} on attempt {}",
                        order.getOrderId(), currentAttempt);
                aggregationService.recordRetry(
                        order.getOrderId(),
                        order.getProduct(),
                        (double) order.getPrice(),
                        topic,
                        currentAttempt,
                        "Simulated temporary inventory lock"
                );
                throw new IllegalStateException("Temporary lock on item: " + order.getProduct());
            } else {
                log.info("[Transient Error Resolved] Order {} successfully unlocked on retry attempt {}",
                        order.getOrderId(), currentAttempt);
            }
        }

        // Valid order -> Process & update real-time running average
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
                3,
                "Exhausted maximum retry attempts (3) or fatal payload validation failure"
        );
    }
}
