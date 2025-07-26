package com.mythicemporium.service;

import com.mythicemporium.dto.CategoryRequestDTO;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
import com.mythicemporium.model.Category;
import com.mythicemporium.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categoryRepository);
    }

    @Test
    void shouldFindAll() {
        when(categoryRepository.findAll()).thenReturn(List.of(generateCategory(1L)));
        List<Category> categorys = service.findAll();

        assertEquals(1, categorys.size());
    }

    @Test
    void shouldFindValidById() {
        when(categoryRepository.findById(any(Long.class))).thenReturn(Optional.of(generateCategory(1L)));
        Category category = service.findById(1L);

        assertNotNull(category);
        assertEquals("Test Category 1", category.getName());
    }

    @Test
    void shouldNotFindInvalidById() {
        when(categoryRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        Category category = service.findById(1L);

        assertNull(category);
    }

    @Test
    void shouldCreateValidCategory() throws ExecutionException, InterruptedException {
        when(categoryRepository.findByName(any(String.class))).thenReturn(List.of());
        when(categoryRepository.save(any(Category.class))).thenReturn(generateCategory(1L));

        CategoryRequestDTO dto = generateCategoryRequest();

        CompletableFuture<Result> result = service.createCategory(dto);

        assertTrue(result.isDone());
        assertNotNull(result.get());
        assertTrue(result.get().isSuccess());

        assertEquals(dto.getName(), ((Category) result.get().getData()).getName());
    }

    @Test
    void shouldNotCreateCategoryWithInvalidDTO() {
        assertThrows(InvalidRequestException.class, () -> service.createCategory(null));
    }

    @Test
    void shouldNotCreateCategoryWithInvalidName() {
        CategoryRequestDTO dto = generateCategoryRequest();

        // Test null name
        dto.setName(null);
        assertThrows(InvalidRequestException.class, () -> service.createCategory(dto));

        // Test empty name
        dto.setName("");
        assertThrows(InvalidRequestException.class, () -> service.createCategory(dto));
    }

    @Test
    void shouldNotCreateCategoryWithDuplicateName() {
        when(categoryRepository.findByName(any(String.class))).thenReturn(List.of(generateCategory(1L)));

        CategoryRequestDTO dto = generateCategoryRequest();

        assertThrows(ResourceConflictException.class, () -> service.createCategory(dto));
    }

    @Test
    void shouldUpdateValidCategory() throws ExecutionException, InterruptedException {
        when(categoryRepository.findById(any(Long.class))).thenReturn(Optional.of(generateCategory(1L)));
        when(categoryRepository.findByName(any(String.class))).thenReturn(List.of());

        CategoryRequestDTO dto = generateCategoryRequest();

        CompletableFuture<Result> result = service.updateCategory(1L, dto);

        assertTrue(result.isDone());
        assertNotNull(result.get());
        assertTrue(result.get().isSuccess());
        assertEquals(dto.getName(), ((Category) result.get().getData()).getName());
    }

    @Test
    void shouldNotUpdateCategoryWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.updateCategory(-1L, generateCategoryRequest()));
    }

    @Test
    void shouldNotUpdateCategoryWithInvalidDTO() {
        assertThrows(ResourceNotFoundException.class, () -> service.updateCategory(0L, null));
    }

    @Test
    void shouldNotUpdateCategoryWithInvalidName() {
        when(categoryRepository.findById(any(Long.class))).thenReturn(Optional.of(generateCategory(1L)));

        CategoryRequestDTO dto = generateCategoryRequest();

        // Test null name;
        dto.setName(null);
        assertThrows(InvalidRequestException.class, () -> service.updateCategory(0L, dto));

        // Test empty name;
        dto.setName("");
        assertThrows(InvalidRequestException.class, () -> service.updateCategory(0L, dto));
    }

    @Test
    void shouldNotUpdateCategoryWithDuplicateName() {
        when(categoryRepository.findById(any(Long.class))).thenReturn(Optional.of(generateCategory(1L)));
        when(categoryRepository.findByName(any(String.class))).thenReturn(List.of(generateCategory(1L)));

        CategoryRequestDTO dto = generateCategoryRequest();

        assertThrows(ResourceConflictException.class, () -> service.updateCategory(0L, dto));
    }

    @Test
    void shouldDeleteValidCategory() {
        when(categoryRepository.deleteByIdAndReturnCount(any(Long.class))).thenReturn(1);

        assertTrue(service.deleteCategory(1L));
    }

    @Test
    void shouldNotDeleteCategoryWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.deleteCategory(-1L));
    }

    @Test
    void shouldNotDeleteCategoryWithNonexistentId() {
        when(categoryRepository.deleteByIdAndReturnCount(any(Long.class))).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteCategory(1L));
    }

    private Category generateCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Test Category " + id);
        return category;
    }

    private CategoryRequestDTO generateCategoryRequest() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Test Category 1");
        return dto;
    }
}
