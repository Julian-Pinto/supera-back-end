package com.supera.Super.A.service;

import com.supera.Super.A.model.Order;
import com.supera.Super.A.model.OrderItem;
import com.supera.Super.A.model.OrderState;
import com.supera.Super.A.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void createOrderShouldSendEmailWithOrderDetails() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        OrderEmailService orderEmailService = mock(OrderEmailService.class);
        OrderService orderService = new OrderService(orderRepository, orderEmailService);

        Order order = new Order();
        order.setCustomerName("Julian");
        order.setPhone("3000000000");
        order.setTower("Torre 1");
        order.setApartment("101");
        order.setItems(List.of(new OrderItem("p1", "Arroz", "Alimentos", 2, 5000.0)));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order savedOrder = orderService.createOrder(order);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderEmailService).sendOrderConfirmation(orderCaptor.capture());
        Order sentOrder = orderCaptor.getValue();

        assertEquals(OrderState.CREATED, savedOrder.getState());
        assertEquals("Julian", sentOrder.getCustomerName());
        assertEquals(10000.0, sentOrder.getTotal(), 0.01);
    }
}
