package com.supera.Super.A.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supera.Super.A.dto.BulkProductItem;
import com.supera.Super.A.dto.BulkUploadResult;
import com.supera.Super.A.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class BulkProductService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String productCreationUrl;

    public BulkProductService(@Value("${bulk.product.creation.url:http://18.189.26.234:8080/api/products}") String productCreationUrl) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.productCreationUrl = productCreationUrl;
    }

    public BulkUploadResult uploadProducts() throws IOException {
        var resource = getClass().getClassLoader().getResource("assets/products.json");
        if (resource == null) {
            throw new IOException("No se encontró el archivo assets/products.json");
        }

        List<BulkProductItem> items = objectMapper.readValue(resource.openStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, BulkProductItem.class));

        BulkUploadResult result = new BulkUploadResult();
        result.setTotal(items.size());

        for (BulkProductItem item : items) {
            try {
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("name", item.getName());
                body.add("category", item.getCategory());
                body.add("price", String.valueOf(item.getPrice()));
                body.add("description", item.getDescription());
                body.add("imageName", item.getImageName());
                body.add("available", String.valueOf(item.isAvailable()));
                body.add("idInvoice", item.getIdInvoice());
                body.add("profitMargin", String.valueOf(item.getProfitMargin()));
                body.add("quantity", String.valueOf(item.getQuantity()));
                body.add("expirationDate", item.getExpirationDate());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
                ResponseEntity<ProductResponse> response = restTemplate.postForEntity(productCreationUrl, request, ProductResponse.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    result.setSuccess(result.getSuccess() + 1);
                } else {
                    result.setFailed(result.getFailed() + 1);
                    result.addError("Failed to create product " + item.getName() + ": status " + response.getStatusCode());
                }
            } catch (Exception e) {
                result.setFailed(result.getFailed() + 1);
                result.addError("Error creating product " + item.getName() + ": " + e.getMessage());
            }
        }

        return result;
    }
}
