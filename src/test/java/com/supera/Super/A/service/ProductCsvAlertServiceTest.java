package com.supera.Super.A.service;

import com.supera.Super.A.model.Product;
import com.supera.Super.A.repository.ProductRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductCsvAlertServiceTest {

    @Test
    void exportProductsToCsvAndSendEmailShouldCreateCsvAndSendAttachment() throws Exception {
        ProductRepository productRepository = mock(ProductRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        Product product = new Product();
        product.setQuantity(5);
        product.setName("Arroz");
        product.setCategory("Alimentos");
        product.setPrice(1000.0);
        product.setProfitMargin(25.0);
        product.setExpirationDate("2026-12-31");
        product.setPriceWithProfit(1250.0);

        when(productRepository.findAll()).thenReturn(List.of(product));

        ProductCsvAlertService service = new ProductCsvAlertService(productRepository, mailSender, "alert@test.com");

        Path csvPath = service.exportProductsToCsvAndSendEmail();

        assertNotNull(csvPath);
        assertTrue(Files.exists(csvPath));
        String content = Files.readString(csvPath);
        assertTrue(content.contains("Cantidad,Producto,Categoria,Valor de compra,porcentaje de ganancia,Fecha de vencimiento,Valor de venta"));
        assertTrue(content.contains("5,Arroz,Alimentos,1000.0,25.0,2026-12-31,1250.0"));

        verify(mailSender).send(any(MimeMessage.class));
    }
}
