package com.ordering.order.dto;

public class BatchOrderRequest {

    private int count = 10;
    private long delayMs = 500;
    private String mode = "normal"; // normal, transient, fatal, mixed

    public BatchOrderRequest() {}

    public BatchOrderRequest(int count, long delayMs, String mode) {
        this.count = count;
        this.delayMs = delayMs;
        this.mode = mode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
