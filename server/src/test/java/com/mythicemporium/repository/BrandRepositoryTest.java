package com.mythicemporium.repository;

import com.mythicemporium.model.Brand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BrandRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BrandRepository brandRepository;

    @Test
    void shouldSave() {
        Brand brand = createTestBrand("Test Brand");

        Brand saved = brandRepository.save(brand);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Brand", saved.getName());
    }

    @Test
    void shouldFindByName() {
        Brand brand = createTestBrand("Test Brand");

        List<Brand> empty = brandRepository.findByName("Empty");
        List<Brand> brands = brandRepository.findByName("Test Brand");

        assertEquals(0, empty.size());
        assertEquals(1, brands.size());
        assertEquals("Test Brand", brands.get(0).getName());
    }

    @Test
    void shouldDeleteByIdAndReturnCount() {
        Brand brand = createTestBrand("Test Brand");

        Brand saved = brandRepository.save(brand);
        Optional<Brand> found = brandRepository.findById(saved.getId());

        assertNotNull(saved.getId());
        assertTrue(found.isPresent());

        int removeCount = brandRepository.deleteByIdAndReturnCount(saved.getId());
        entityManager.clear();
        Optional<Brand> foundRemoved = brandRepository.findById(saved.getId());

        assertEquals(1, removeCount);
        assertTrue(foundRemoved.isEmpty());
    }
}
