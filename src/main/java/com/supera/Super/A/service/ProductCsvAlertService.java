package com.supera.Super.A.service;

import com.supera.Super.A.model.Product;
import com.supera.Super.A.repository.ProductRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductCsvAlertService {

    private final ProductRepository productRepository;
    private final JavaMailSender mailSender;

    @Value("${alerts.email.recipients:julianpintoramos88@gmail.com}")
    private String recipients = "julianpintoramos88@gmail.com";

    public ProductCsvAlertService(ProductRepository productRepository, JavaMailSender mailSender) {
        this.productRepository = productRepository;
        this.mailSender = mailSender;
    }

    public Path exportProductsToCsvAndSendEmail() throws IOException, MessagingException {
        List<Product> products = productRepository.findAll();
        Path exportPath = Paths.get("target", "products-alerts.csv");

        String csvContent = buildCsvContent(products);
        Files.writeString(exportPath, csvContent, StandardCharsets.UTF_8);

        sendEmailWithAttachment(exportPath);
        return exportPath;
    }

    private String buildCsvContent(List<Product> products) {
        String header = "Cantidad,Producto,Categoria,Valor de compra,porcentaje de ganancia,Fecha de vencimiento,Valor de venta";
        String rows = products.stream()
                .map(this::toCsvRow)
                .collect(Collectors.joining(System.lineSeparator()));

        return header + System.lineSeparator() + rows + (rows.isEmpty() ? "" : System.lineSeparator());
    }

    private String toCsvRow(Product product) {
        return String.join(",",
                String.valueOf(product.getQuantity()),
                escapeCsv(product.getName()),
                escapeCsv(product.getCategory()),
                String.valueOf(product.getPrice()),
                String.valueOf(product.getProfitMargin()),
                escapeCsv(product.getExpirationDate()),
                String.valueOf(product.getPriceWithProfit())
        );
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", "\\,");
    }

    private void sendEmailWithAttachment(Path attachmentPath) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        String recipientsValue = (recipients == null || recipients.isBlank())
                ? "julianpintoramos88@gmail.com"
                : recipients;

        helper.setFrom("noreply@supera.com");
        helper.setTo(recipientsValue.split(","));
        helper.setSubject("Alertas de productos - CSV");
        helper.setText("Adjunto encontrará el archivo CSV con la información de productos.");
        helper.addAttachment("productos.csv", new FileSystemResource(attachmentPath));
        mailSender.send(message);
    }
}
