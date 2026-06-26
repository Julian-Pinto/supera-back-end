package com.supera.Super.A.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CustomerRequest {

    @NotBlank
    private String name;

    private String tower;

    private String apartment;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTower() {
        return tower;
    }

    public void setTower(String tower) {
        this.tower = tower;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
