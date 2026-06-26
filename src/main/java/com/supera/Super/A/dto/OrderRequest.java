package com.supera.Super.A.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {

    @NotNull
    @Valid
    private CustomerRequest customer;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<OrderItemRequest> items;

    private BigDecimal total;

    public CustomerRequest getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerRequest customer) {
        this.customer = customer;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
