package com.supera.Super.A.controller;

import com.supera.Super.A.dto.OrderRequest;
import com.supera.Super.A.dto.OrderStatusUpdateRequest;
import jakarta.validation.Valid;
import com.supera.Super.A.model.Order;
import com.supera.Super.A.model.OrderItem;
import com.supera.Super.A.model.OrderState;
import com.supera.Super.A.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        // Map DTO -> Entity (null-safe) and recalculate total server-side
        Order order = new Order();
        order.setCustomerName(orderRequest.getCustomer().getName());
        order.setPhone(orderRequest.getCustomer().getPhone());
        order.setTower(orderRequest.getCustomer().getTower());
        order.setApartment(orderRequest.getCustomer().getApartment());

        double total = 0.0;
        List<OrderItem> items = new java.util.ArrayList<>();
        for (var itReq : orderRequest.getItems()) {
            OrderItem it = new OrderItem();
            it.setProductId(itReq.getProductId());
            int qty = itReq.getQuantity() == null ? 0 : itReq.getQuantity();
            double price = itReq.getPrice() == null ? 0.0 : itReq.getPrice().doubleValue();
            it.setAmount(qty);
            it.setUnitPrice(price);
            it.setSubTotal(qty * price);
            total += qty * price;
            items.add(it);
        }
        order.setItems(items);
        order.setTotal(total);
        Order saved = orderService.createOrder(order);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable String id, @RequestBody Order order) {
        return orderService.update(id, order)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable String id,
                                                   @RequestBody OrderStatusUpdateRequest request) {
        OrderState newState = OrderState.valueOf(request.getState());
        return orderService.updateStatus(id, newState)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
