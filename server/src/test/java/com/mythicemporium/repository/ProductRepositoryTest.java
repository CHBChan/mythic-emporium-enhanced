package com.mythicemporium.repository;

import com.mythicemporium.model.Brand;
import com.mythicemporium.model.Category;
import com.mythicemporium.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryTest extends BaseRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariationRepository productVariationRepository;

    private Brand testBrand;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testBrand = createTestBrand("Test Brand");
        testCategory = createTestCategory("Test Category");
    }

    @Test
    void shouldSave() {
        Product product = createTestProduct("Test Product", "Test Description", testBrand, testCategory);

        Product saved = productRepository.save(product);
        Optional<Product> found = productRepository.findById(saved.getId());

        assertNotNull(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Product", found.get().getName());
        assertEquals("Test Description", found.get().getDescription());
        assertEquals("Test Brand", found.get().getBrand().getName());
        assertEquals("Test Category", found.get().getCategory().getName());
    }

    @Test
    void shouldReturnAllByBrand() {
        Brand testBrand2 = createTestBrand("Test Brand 2");

        Product product1 = createTestProduct("Test Product 1", "Test Description", testBrand, testCategory);
        Product product2 = createTestProduct("Test Product 2", "Test Description", testBrand, testCategory);
        Product product3 = createTestProduct("Test Product 3", "Test Description", testBrand2, testCategory);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        List<Product> allProducts = productRepository.findAll();
        List<Product> testBrandProducts = productRepository.findAllByBrandId(testBrand.getId());
        List<Product> testBrand2Products = productRepository.findAllByBrandId(testBrand2.getId());

        assertEquals(3, allProducts.size());
        assertEquals(2, testBrandProducts.size());
        assertEquals(1, testBrand2Products.size());
    }

    @Test
    void shouldReturnAllByCategory() {
        Category testCategory2 = createTestCategory("Test Category 2");

        Product product1 = createTestProduct("Test Product 1", "Test Description", testBrand, testCategory);
        Product product2 = createTestProduct("Test Product 2", "Test Description", testBrand, testCategory);
        Product product3 = createTestProduct("Test Product 3", "Test Description", testBrand, testCategory2);

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);

        List<Product> allProducts = productRepository.findAll();
        List<Product> testCategoryProducts = productRepository.findAllByCategoryId(testCategory.getId());
        List<Product> testCategory2Products = productRepository.findAllByCategoryId(testCategory2.getId());

        assertEquals(3, allProducts.size());
        assertEquals(2, testCategoryProducts.size());
        assertEquals(1, testCategory2Products.size());
    }

    @Test
    void shouldDeleteByIdAndReturnCount() {
        Product product = createTestProduct("Test Product", "Test Description", testBrand, testCategory);

        Product saved = productRepository.save(product);
        Optional<Product> found = productRepository.findById(saved.getId());

        assertNotNull(saved.getId());
        assertTrue(found.isPresent());

        int removeCount = productRepository.deleteByIdAndReturnCount(saved.getId());
        entityManager.clear();
        Optional<Product> foundRemoved = productRepository.findById(saved.getId());

        assertEquals(1, removeCount);
        assertTrue(foundRemoved.isEmpty());
    }
}