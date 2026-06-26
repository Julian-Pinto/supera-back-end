package com.supera.Super.A.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private String customerName;
    private String phone;
    private String tower;
    private String apartment;
    private Date createDate;
    private OrderState state;
    private double total;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(String id, String customerName, String phone, String tower, String apartment, Date createDate, OrderState state, double total, List<OrderItem> items) {
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.tower = tower;
        this.apartment = apartment;
        this.createDate = createDate;
        this.state = state;
        this.total = total;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
