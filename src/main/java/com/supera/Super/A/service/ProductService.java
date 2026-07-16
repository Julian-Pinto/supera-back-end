package com.supera.Super.A.service;

import com.supera.Super.A.dto.BulkStockDecreaseRequest;
import com.supera.Super.A.model.Product;
import com.supera.Super.A.dto.ProductResponse;
import com.supera.Super.A.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(p -> new ProductResponse(
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getDescription(),
                p.getImageUrl(),
                p.isAvailable(),
                p.getIdInvoice(),
                p.getQuantity(),
                p.getPriceWithProfit()
        )).toList();
    }

    // Admin: return full product entities with all fields
    public List<Product> findAllAdmin() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product update(String id, Product product) {
        return productRepository.findById(id)
                .map(existing -> {
                            existing.setName(product.getName());
                            existing.setCategory(product.getCategory());
                            existing.setPrice(product.getPrice());
                            existing.setDescription(product.getDescription());
                            existing.setImageUrl(product.getImageUrl());
                            existing.setQuantity(product.getQuantity());
                            existing.setExpirationDate(product.getExpirationDate());
                            // compute priceWithProfit from provided price and profitMargin
                            existing.setPriceWithProfit(product.getPrice() + (product.getPrice() * product.getProfitMargin() / 100.0));
                            existing.setAvailable(product.isAvailable());
                            existing.setIdInvoice(product.getIdInvoice());
                            existing.setProfitMargin(product.getProfitMargin());
                            return productRepository.save(existing);
                        })
                .orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));
    }

    public boolean deleteById(String id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.deleteById(product.getId());
                    return true;
                })
                .orElse(false);
    }

    public Optional<Product> updateAvailability(String id, boolean available) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setAvailable(available);
                    return productRepository.save(product);
                });
    }

    public List<Product> decreaseStockInBatch(List<BulkStockDecreaseRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("La lista de productos no puede estar vacía");
        }

        List<String> productIds = requests.stream()
                .map(BulkStockDecreaseRequest::getProductId)
                .toList();

        List<Product> products = new ArrayList<>(productRepository.findAllById(productIds));

        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("Uno o más productId no existen");
        }

        for (BulkStockDecreaseRequest request : requests) {
            Product product = products.stream()
                    .filter(item -> request.getProductId().equals(item.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + request.getProductId()));

            if (request.getQuantityToSubtract() <= 0) {
                throw new IllegalArgumentException("quantityToSubtract debe ser mayor que 0");
            }

            int currentQuantity = product.getQuantity();
            if (currentQuantity < request.getQuantityToSubtract()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + request.getProductId());
            }

            int updatedQuantity = currentQuantity - request.getQuantityToSubtract();
            product.setQuantity(updatedQuantity);
            product.setAvailable(updatedQuantity > 0);
            productRepository.save(product);
        }

        return products;
    }

}
