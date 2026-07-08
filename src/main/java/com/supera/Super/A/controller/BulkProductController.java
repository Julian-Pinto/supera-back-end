package com.supera.Super.A.controller;

import com.supera.Super.A.dto.BulkUploadResult;
import com.supera.Super.A.service.BulkProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/bulk-products")
public class BulkProductController {

    private final BulkProductService bulkProductService;

    public BulkProductController(BulkProductService bulkProductService) {
        this.bulkProductService = bulkProductService;
    }

    @GetMapping("/upload")
    public ResponseEntity<?> uploadBulkProducts() {
        try {
            BulkUploadResult result = bulkProductService.uploadProducts();
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body("No se pudo leer el archivo JSON: " + e.getMessage());
        }
    }
}
