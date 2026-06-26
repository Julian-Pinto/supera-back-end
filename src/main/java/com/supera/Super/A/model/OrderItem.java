package com.supera.Super.A.model;

public class OrderItem {

    private String productId;
    private String name;
    private String category;
    private int amount;
    private double unitPrice;
    private double subTotal;

    public OrderItem() {
    }

    public OrderItem(String productId, String name, String category, int amount, double unitPrice) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.subTotal = amount * unitPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.subTotal = this.unitPrice * amount;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subTotal = this.amount * unitPrice;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }
}
