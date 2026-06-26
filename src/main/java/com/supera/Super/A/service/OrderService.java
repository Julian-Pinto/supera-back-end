package com.supera.Super.A.service;

import com.supera.Super.A.model.Order;
import com.supera.Super.A.model.OrderState;
import com.supera.Super.A.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Order order) {
        order.setCreateDate(new Date());
        order.setState(OrderState.CREATED);
        order.setTotal(calculateTotal(order));
        return orderRepository.save(order);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> updateStatus(String id, OrderState state) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setState(state);
                    return orderRepository.save(order);
                });
    }

    public Optional<Order> update(String id, Order order) {
        return orderRepository.findById(id)
                .map(existing -> {
                    existing.setCustomerName(order.getCustomerName());
                    existing.setPhone(order.getPhone());
                    existing.setTower(order.getTower());
                    existing.setApartment(order.getApartment());
                    existing.setItems(order.getItems());
                    existing.setTotal(calculateTotal(existing));
                    return orderRepository.save(existing);
                });
    }

    private double calculateTotal(Order order) {
        if (order.getItems() == null) {
            return 0.0;
        }
        return order.getItems().stream()
                .mapToDouble(item -> {
                    double subTotal = item.getUnitPrice() * item.getAmount();
                    item.setSubTotal(subTotal);
                    return subTotal;
                })
                .sum();
    }
}
