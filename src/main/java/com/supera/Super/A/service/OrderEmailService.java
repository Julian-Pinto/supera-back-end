package com.supera.Super.A.service;

import com.supera.Super.A.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class OrderEmailService {

    private static final String DESTINATION_EMAIL = "julianpintoramos88@gmail.com";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public OrderEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(Order order) {
        if (order == null) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail.isBlank() ? "noreply@supera.com" : fromEmail);
        message.setTo(DESTINATION_EMAIL);
        message.setSubject("Nuevo pedido recibido - SuperA");
        message.setText(buildEmailBody(order));
        mailSender.send(message);
    }

    private String buildEmailBody(Order order) {
        StringBuilder body = new StringBuilder();
        body.append("Hola,\n\n");
        body.append("Se ha registrado un nuevo pedido en SuperA.\n\n");
        body.append("Cliente: ").append(order.getCustomerName()).append("\n");
        body.append("Teléfono: ").append(order.getPhone()).append("\n");
        body.append("Torre: ").append(order.getTower()).append("\n");
        body.append("Apartamento: ").append(order.getApartment()).append("\n\n");
        body.append("Productos:\n");

        if (order.getItems() == null || order.getItems().isEmpty()) {
            body.append("- Sin productos\n");
        } else {
            for (var item : order.getItems()) {
                body.append("- ")
                        .append(item.getName() != null ? item.getName() : "Producto")
                        .append(" | Cantidad: ")
                        .append(item.getAmount())
                        .append(" | Valor unitario: $")
                        .append(formatCurrency(item.getUnitPrice()))
                        .append(" | Subtotal: $")
                        .append(formatCurrency(item.getSubTotal()))
                        .append("\n");
            }
        }

        body.append("\nTotal del pedido: $").append(formatCurrency(order.getTotal())).append("\n");
        body.append("Fecha: ").append(formatDate(order.getCreateDate())).append("\n");
        return body.toString();
    }

    private String formatCurrency(double value) {
        return new DecimalFormat("#.##").format(value);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "No disponible";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }
}
