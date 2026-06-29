package com.supera.Super.A.dto;

public class ProductResponse {
    private String id;
    private String name;
    private String category;
    private String description;
    private String imageUrl;
    private boolean available;
    private String idInvoice;
    private int quantity;
    private double priceWithProfit;

    public ProductResponse() {
    }

    public ProductResponse(String id, String name, String category, String description, String imageUrl, boolean available, String idInvoice, int quantity, double priceWithProfit) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.imageUrl = imageUrl;
        this.available = available;
        this.idInvoice = idInvoice;
        this.quantity = quantity;
        this.priceWithProfit = priceWithProfit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getIdInvoice() {
        return idInvoice;
    }

    public void setIdInvoice(String idInvoice) {
        this.idInvoice = idInvoice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceWithProfit() {
        return priceWithProfit;
    }

    public void setPriceWithProfit(double priceWithProfit) {
        this.priceWithProfit = priceWithProfit;
    }
}
