package com.mythicemporium.repository;

import com.mythicemporium.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldSave() {
        Category category = createTestCategory("Test Category");

        Category saved = categoryRepository.save(category);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Category", saved.getName());
    }

    @Test
    void shouldFindByName() {
        Category category = createTestCategory("Test Category");

        List<Category> empty = categoryRepository.findByName("Empty");
        List<Category> categorys = categoryRepository.findByName("Test Category");

        assertEquals(0, empty.size());
        assertEquals(1, categorys.size());
        assertEquals("Test Category", categorys.get(0).getName());
    }

    @Test
    void shouldDeleteByIdAndReturnCount() {
        Category category = createTestCategory("Test Category");

        Category saved = categoryRepository.save(category);
        Optional<Category> found = categoryRepository.findById(saved.getId());

        assertNotNull(saved.getId());
        assertTrue(found.isPresent());

        int removeCount = categoryRepository.deleteByIdAndReturnCount(saved.getId());
        entityManager.clear();
        Optional<Category> foundRemoved = categoryRepository.findById(saved.getId());

        assertEquals(1, removeCount);
        assertTrue(foundRemoved.isEmpty());
    }
}
