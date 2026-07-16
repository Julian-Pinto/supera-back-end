package com.supera.Super.A.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BulkStockDecreaseRequest {

    @NotBlank(message = "productId es obligatorio")
    private String productId;

    @Min(value = 1, message = "quantityToSubtract debe ser mayor que 0")
    private int quantityToSubtract;

    public BulkStockDecreaseRequest() {
    }

    public BulkStockDecreaseRequest(String productId, int quantityToSubtract) {
        this.productId = productId;
        this.quantityToSubtract = quantityToSubtract;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantityToSubtract() {
        return quantityToSubtract;
    }

    public void setQuantityToSubtract(int quantityToSubtract) {
        this.quantityToSubtract = quantityToSubtract;
    }
}
