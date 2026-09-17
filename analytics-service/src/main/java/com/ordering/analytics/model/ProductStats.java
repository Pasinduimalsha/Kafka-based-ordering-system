package com.ordering.analytics.model;

public class ProductStats {

    private long count = 0;
    private double totalRevenue = 0.0;
    private double minPrice = Double.MAX_VALUE;
    private double maxPrice = Double.MIN_VALUE;

    public synchronized void recordOrder(double price) {
        this.count++;
        this.totalRevenue += price;
        if (price < this.minPrice) {
            this.minPrice = price;
        }
        if (price > this.maxPrice) {
            this.maxPrice = price;
        }
    }

    public synchronized long getCount() {
        return count;
    }

    public synchronized double getTotalRevenue() {
        return Math.round(totalRevenue * 100.0) / 100.0;
    }

    public synchronized double getAveragePrice() {
        if (count == 0) return 0.0;
        return Math.round((totalRevenue / count) * 100.0) / 100.0;
    }

    public synchronized double getMinPrice() {
        return count == 0 ? 0.0 : Math.round(minPrice * 100.0) / 100.0;
    }

    public synchronized double getMaxPrice() {
        return count == 0 ? 0.0 : Math.round(maxPrice * 100.0) / 100.0;
    }
}
