package com.ordering.analytics.dto;

import java.time.Instant;

public class OrderEventDto {

    private String orderId;
    private String product;
    private Double price;
    private String status; // PROCESSED, RETRYING, DLQ, FAILED
    private String topic;
    private Integer retryCount;
    private String details;
    private String timestamp;

    public OrderEventDto() {
        this.timestamp = Instant.now().toString();
    }

    public OrderEventDto(String orderId, String product, Double price, String status, String topic, Integer retryCount, String details) {
        this.orderId = orderId;
        this.product = product;
        this.price = price;
        this.status = status;
        this.topic = topic;
        this.retryCount = retryCount;
        this.details = details;
        this.timestamp = Instant.now().toString();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
