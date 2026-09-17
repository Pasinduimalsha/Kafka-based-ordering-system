package com.ordering.analytics;

import com.ordering.analytics.dto.AnalyticsSummaryDto;
import com.ordering.analytics.service.AggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregationServiceTest {

    private AggregationService aggregationService;

    @BeforeEach
    void setUp() {
        aggregationService = new AggregationService();
    }

    @Test
    void testInitialMetrics() {
        AnalyticsSummaryDto summary = aggregationService.getSummary();
        assertEquals(0, summary.getTotalOrders());
        assertEquals(0.0, summary.getRunningAveragePrice());
        assertEquals(0.0, summary.getTotalRevenue());
    }

    @Test
    void testRunningAverageCalculation() {
        // Order 1: 100.0 -> Avg: 100.0
        aggregationService.recordValidOrder("1001", "Laptop", 100.0, "orders");
        assertEquals(1, aggregationService.getSummary().getTotalOrders());
        assertEquals(100.0, aggregationService.getRunningAveragePrice());

        // Order 2: 200.0 -> Avg: 150.0
        aggregationService.recordValidOrder("1002", "Phone", 200.0, "orders");
        assertEquals(2, aggregationService.getSummary().getTotalOrders());
        assertEquals(150.0, aggregationService.getRunningAveragePrice());
        assertEquals(300.0, aggregationService.getSummary().getTotalRevenue());

        // Order 3: 300.0 -> Avg: 200.0
        aggregationService.recordValidOrder("1003", "Laptop", 300.0, "orders");
        assertEquals(3, aggregationService.getSummary().getTotalOrders());
        assertEquals(200.0, aggregationService.getRunningAveragePrice());
        assertEquals(600.0, aggregationService.getSummary().getTotalRevenue());
    }

    @Test
    void testPerProductAggregation() {
        aggregationService.recordValidOrder("1001", "Keyboard", 50.0, "orders");
        aggregationService.recordValidOrder("1002", "Keyboard", 150.0, "orders");
        aggregationService.recordValidOrder("1003", "Mouse", 40.0, "orders");

        AnalyticsSummaryDto summary = aggregationService.getSummary();
        assertEquals(3, summary.getTotalOrders());

        AnalyticsSummaryDto.ProductMetricsDto kbStats = summary.getPerProduct().get("Keyboard");
        assertEquals(2, kbStats.getCount());
        assertEquals(100.0, kbStats.getAveragePrice());
        assertEquals(200.0, kbStats.getTotalRevenue());

        AnalyticsSummaryDto.ProductMetricsDto mouseStats = summary.getPerProduct().get("Mouse");
        assertEquals(1, mouseStats.getCount());
        assertEquals(40.0, mouseStats.getAveragePrice());
    }

    @Test
    void testRetryAndDlqCounters() {
        aggregationService.recordRetry("1001", "LockedItem", 99.0, "orders", 1, "Temporary Lock");
        assertEquals(1, aggregationService.getSummary().getTotalRetries());

        aggregationService.recordDlq("1002", "FatalItem", -10.0, "orders-dlq", 3, "Exhausted retries");
        assertEquals(1, aggregationService.getSummary().getTotalDlqMessages());
    }
}
