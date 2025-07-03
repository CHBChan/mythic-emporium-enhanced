package com.mythicemporium.repository;

import com.mythicemporium.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;

@DataJpaTest
@Testcontainers
public class BaseRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
    }

    @Autowired
    protected TestEntityManager entityManager;

    protected Brand createTestBrand(String name) {
        Brand brand = new Brand();
        brand.setName(name);
        return entityManager.persistAndFlush(brand);
    }

    protected Category createTestCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return entityManager.persistAndFlush(category);
    }

    protected Product createTestProduct(String name, String description, Brand brand, Category category) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setBrand(brand);
        product.setCategory(category);
        return entityManager.persistAndFlush(product);
    }

    protected ProductVariation createTestVariation(String sku, Double price, Integer stock, String imageUrl) {
        ProductVariation variation = new ProductVariation();
        variation.setSku(sku);
        variation.setPrice(price);
        variation.setStock(stock);
        variation.setImageUrl(imageUrl);
        return variation;
    }

    protected ProductVariationAttribute createTestAttribute(String name, String value) {
        ProductVariationAttribute attribute = new ProductVariationAttribute();
        attribute.setAttributeName(name);
        attribute.setAttributeValue(value);
        return attribute;
    }
}
