package com.supera.Super.A.service;

import com.supera.Super.A.dto.BulkStockDecreaseRequest;
import com.supera.Super.A.model.Product;
import com.supera.Super.A.repository.ProductRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    @Test
    void decreaseStockInBatchShouldReduceQuantityForExistingProducts() {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductService productService = new ProductService(productRepository);

        Product product = new Product();
        product.setId("p1");
        product.setQuantity(10);

        BulkStockDecreaseRequest request = new BulkStockDecreaseRequest("p1", 3);

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.decreaseStockInBatch(List.of(request));

        assertEquals(7, product.getQuantity());
        verify(productRepository).save(product);
    }
}
