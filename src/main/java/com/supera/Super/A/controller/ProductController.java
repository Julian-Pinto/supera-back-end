package com.supera.Super.A.controller;

import com.supera.Super.A.model.Category;
import com.supera.Super.A.model.Product;
import com.supera.Super.A.service.CategoryService;
import com.supera.Super.A.service.ProductCsvAlertService;
import com.supera.Super.A.service.ProductService;
import com.supera.Super.A.service.S3ImageService;
import com.supera.Super.A.dto.AvailabilityUpdateRequest;
import com.supera.Super.A.dto.BulkStockDecreaseRequest;
import com.supera.Super.A.dto.ProductCreateRequest;
import com.supera.Super.A.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static java.lang.Math.ceil;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final S3ImageService s3ImageService;
    private final ProductCsvAlertService productCsvAlertService;

    public ProductController(ProductService productService, CategoryService categoryService, S3ImageService s3ImageService, ProductCsvAlertService productCsvAlertService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.s3ImageService = s3ImageService;
        this.productCsvAlertService = productCsvAlertService;
    }

    @GetMapping
    public List<com.supera.Super.A.dto.ProductResponse> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/admin")
    public List<com.supera.Super.A.model.Product> getAllProductsAdmin() {
        return productService.findAllAdmin();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("price") double price,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "imageName", required = false) String imageName,
            @RequestParam("available") boolean available,
            @RequestParam(value = "idInvoice", required = false) String idInvoice,
            @RequestParam(value = "profitMargin", defaultValue = "0") double profitMargin,
            @RequestParam(value = "quantity", defaultValue = "0") int quantity,
            @RequestParam(value = "expirationDate", required = false) String expirationDate) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setDescription(description);
            product.setQuantity(quantity);
            product.setExpirationDate(expirationDate);
            product.setAvailable(available);
            product.setIdInvoice(idInvoice);
            product.setProfitMargin(profitMargin);
            double priceWithProfit = calculatePriceWithProfit(price, profitMargin);
            product.setPriceWithProfit(priceWithProfit);

            if (image != null && !image.isEmpty()) {
                String imageUrl = s3ImageService.uploadImageUrl(image);
                product.setImageUrl(imageUrl);
            } else if (imageName != null && !imageName.isBlank()) {
                product.setImageUrl(s3ImageService.getImageUrl(imageName));
            }

            Product saved = productService.save(product);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la imagen: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("La categoría ya existe"));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        return categoryService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        return productService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> updateProduct(
            @PathVariable String id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "imageName", required = false) String imageName,
            @RequestParam(value = "available", required = false) Boolean available,
            @RequestParam(value = "idInvoice", required = false) String idInvoice,
            @RequestParam(value = "profitMargin", required = false) Double profitMargin,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "expirationDate", required = false) String expirationDate,
            @RequestBody(required = false) ProductUpdateRequest updateRequest) {
        try {
            return productService.findById(id)
                    .map(existingProduct -> {
                        ProductUpdateRequest request = updateRequest != null ? updateRequest : new ProductUpdateRequest();

                        if (name != null) existingProduct.setName(name);
                        else if (request.getName() != null) existingProduct.setName(request.getName());

                        if (category != null) existingProduct.setCategory(category);
                        else if (request.getCategory() != null) existingProduct.setCategory(request.getCategory());

                        if (price != null) existingProduct.setPrice(price);
                        else if (request.getPrice() != null) existingProduct.setPrice(request.getPrice());

                        if (description != null) existingProduct.setDescription(description);
                        else if (request.getDescription() != null) existingProduct.setDescription(request.getDescription());

                        if (quantity != null) existingProduct.setQuantity(quantity);
                        else if (request.getQuantity() != null) existingProduct.setQuantity(request.getQuantity());

                        if (expirationDate != null) existingProduct.setExpirationDate(expirationDate);
                        else if (request.getExpirationDate() != null) existingProduct.setExpirationDate(request.getExpirationDate());

                        if (available != null) existingProduct.setAvailable(available);
                        else if (request.getAvailable() != null) existingProduct.setAvailable(request.getAvailable());

                        if (idInvoice != null) existingProduct.setIdInvoice(idInvoice);
                        else if (request.getIdInvoice() != null) existingProduct.setIdInvoice(request.getIdInvoice());

                        if (profitMargin != null) existingProduct.setProfitMargin(profitMargin);
                        else if (request.getProfitMargin() != null) existingProduct.setProfitMargin(request.getProfitMargin());

                        existingProduct.setPriceWithProfit(
                            calculatePriceWithProfit(existingProduct.getPrice(), existingProduct.getProfitMargin())
                        );

                        if (image != null && !image.isEmpty()) {
                            try {
                                String oldImageUrl = existingProduct.getImageUrl();
                                String newImageUrl = s3ImageService.uploadImageUrl(image);
                                existingProduct.setImageUrl(newImageUrl);

                                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                                    s3ImageService.deleteImage(oldImageUrl);
                                }
                            } catch (IOException e) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error al procesar la imagen: " + e.getMessage());
                            }
                        } else if (imageName != null && !imageName.isBlank()) {
                            existingProduct.setImageUrl(s3ImageService.getImageUrl(imageName));
                        }

                        Product updated = productService.save(existingProduct);
                        return ResponseEntity.ok(updated);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    private double calculatePriceWithProfit(double basePrice, double profitMargin) {
        double calculatedPrice = basePrice + (basePrice * profitMargin / 100.0);
        return ceil(calculatedPrice / 100.0) * 100.0;
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<Product> updateAvailability(@PathVariable String id,
                                                      @RequestBody AvailabilityUpdateRequest request) {
        return productService.updateAvailability(id, request.isAvailable())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bulk-decrease-stock")
    public ResponseEntity<Void> decreaseStockInBatch(@Valid @RequestBody List<BulkStockDecreaseRequest> requests) {
        productService.decreaseStockInBatch(requests);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/alerts/export-csv")
    public ResponseEntity<?> exportProductsCsvAndSendEmail() {
        try {
            productCsvAlertService.exportProductsToCsvAndSendEmail();
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo generar o enviar el CSV: " + ex.getMessage());
        }
    }
}
