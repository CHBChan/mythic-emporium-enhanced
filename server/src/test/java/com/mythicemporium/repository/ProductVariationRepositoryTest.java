package com.mythicemporium.repository;

import com.mythicemporium.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductVariationRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariationRepository productVariationRepository;

    private Brand testBrand;
    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testBrand = createTestBrand("Test Brand");
        testCategory = createTestCategory("Test Category");
        testProduct = createTestProduct("Test Product", "Test Description", testBrand, testCategory);
    }

    @Test
    void shouldSave() {
        ProductVariation variation = createTestVariation("Test SKU", 1.99, 4, "img");
        variation.setProduct(testProduct);

        ProductVariation saved = productVariationRepository.save(variation);
        Optional<ProductVariation> found = productVariationRepository.findById(saved.getId());

        assertNotNull(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test SKU", found.get().getSku());
        assertEquals(1.99, found.get().getPrice());
        assertEquals(4, found.get().getStock());
        assertEquals("img", found.get().getImageUrl());
    }

    @Test
    void shouldDecrementStock() {
        ProductVariation variation = createTestVariation("Test SKU", 1.99, 4, "img");
        variation.setProduct(testProduct);

        ProductVariation saved = productVariationRepository.save(variation);
        int decrementedCount = productVariationRepository.decrementStock(saved.getId(), 2);
        entityManager.clear();
        Optional<ProductVariation> decremented = productVariationRepository.findById(saved.getId());

        assertEquals(1, decrementedCount);
        assertNotNull(saved.getId());
        assertTrue(decremented.isPresent());
        assertEquals(2, decremented.get().getStock());
    }

    @Test
    void shouldUpdateStockById() {
        ProductVariation variation = createTestVariation("Test SKU", 1.99, 4, "img");
        variation.setProduct(testProduct);

        ProductVariation saved = productVariationRepository.save(variation);
        int updatedCount = productVariationRepository.updateStockById(saved.getId(), 222);
        entityManager.clear();
        Optional<ProductVariation> updated = productVariationRepository.findById(saved.getId());

        assertEquals(1, updatedCount);
        assertNotNull(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals(222, updated.get().getStock());
    }

    @Test
    void shouldUpdatePriceById() {
        ProductVariation variation = createTestVariation("Test SKU", 1.99, 4, "img");
        variation.setProduct(testProduct);

        ProductVariation saved = productVariationRepository.save(variation);
        int updatedCount = productVariationRepository.updatePriceById(saved.getId(), 2.22);
        entityManager.clear();
        Optional<ProductVariation> updated = productVariationRepository.findById(saved.getId());

        assertEquals(1, updatedCount);
        assertNotNull(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals(2.22, updated.get().getPrice());
    }
}