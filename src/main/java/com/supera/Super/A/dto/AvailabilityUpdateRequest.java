package com.supera.Super.A.dto;

public class AvailabilityUpdateRequest {

    private boolean available;

    public AvailabilityUpdateRequest() {
    }

    public AvailabilityUpdateRequest(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
