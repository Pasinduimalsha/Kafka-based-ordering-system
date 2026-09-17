package com.ordering.order.dto;

import java.time.Instant;

public class OrderResponse {

    private String orderId;
    private String product;
    private Float price;
    private String status;
    private String topic;
    private String timestamp;

    public OrderResponse() {}

    public OrderResponse(String orderId, String product, Float price, String status, String topic) {
        this.orderId = orderId;
        this.product = product;
        this.price = price;
        this.status = status;
        this.topic = topic;
        this.timestamp = Instant.now().toString();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProduct() {
        return product;
    }

    public Float getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public String getTopic() {
        return topic;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
