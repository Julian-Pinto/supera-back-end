package com.supera.Super.A.dto;

public class OrderStatusUpdateRequest {

    private String state;

    public OrderStatusUpdateRequest() {
    }

    public OrderStatusUpdateRequest(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
