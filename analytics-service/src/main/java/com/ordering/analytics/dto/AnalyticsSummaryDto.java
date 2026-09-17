package com.ordering.analytics.dto;

import java.util.Map;

public class AnalyticsSummaryDto {

    private long totalOrders;
    private double totalRevenue;
    private double runningAveragePrice;
    private double minPrice;
    private double maxPrice;
    private long totalRetries;
    private long totalDlqMessages;
    private Map<String, ProductMetricsDto> perProduct;
    private String lastUpdated;

    public AnalyticsSummaryDto() {}

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getRunningAveragePrice() { return runningAveragePrice; }
    public void setRunningAveragePrice(double runningAveragePrice) { this.runningAveragePrice = runningAveragePrice; }

    public double getMinPrice() { return minPrice; }
    public void setMinPrice(double minPrice) { this.minPrice = minPrice; }

    public double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(double maxPrice) { this.maxPrice = maxPrice; }

    public long getTotalRetries() { return totalRetries; }
    public void setTotalRetries(long totalRetries) { this.totalRetries = totalRetries; }

    public long getTotalDlqMessages() { return totalDlqMessages; }
    public void setTotalDlqMessages(long totalDlqMessages) { this.totalDlqMessages = totalDlqMessages; }

    public Map<String, ProductMetricsDto> getPerProduct() { return perProduct; }
    public void setPerProduct(Map<String, ProductMetricsDto> perProduct) { this.perProduct = perProduct; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public static class ProductMetricsDto {
        private long count;
        private double totalRevenue;
        private double averagePrice;
        private double minPrice;
        private double maxPrice;

        public ProductMetricsDto(long count, double totalRevenue, double averagePrice, double minPrice, double maxPrice) {
            this.count = count;
            this.totalRevenue = totalRevenue;
            this.averagePrice = averagePrice;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }

        public long getCount() { return count; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAveragePrice() { return averagePrice; }
        public double getMinPrice() { return minPrice; }
        public double getMaxPrice() { return maxPrice; }
    }
}
