package com.mythicemporium.service;

import com.mythicemporium.dto.CategoryRequestDTO;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
import com.mythicemporium.logging.AuditContext;
import com.mythicemporium.logging.AuditContextHolder;
import com.mythicemporium.model.Category;
import com.mythicemporium.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class CategoryService {

    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);

        return category.orElse(null);
    }

    public CompletableFuture<Result> createCategory(CategoryRequestDTO categoryRequestDTO) {
        if(categoryRequestDTO == null) {
            throw new InvalidRequestException("Category cannot be null.");
        }

        if(categoryRequestDTO.getName() == null || categoryRequestDTO.getName().isBlank()) {
            throw new InvalidRequestException("Category name cannot be null or empty.");
        }

        if(!categoryRepository.findByName(categoryRequestDTO.getName()).isEmpty()) {
            throw new ResourceConflictException("Category name already exists.");
        }

        Category category = new Category();
        category.setName(categoryRequestDTO.getName());

        Result result = new Result();

        try {
            category = categoryRepository.save(category);

            AuditContext ctx = AuditContextHolder.getContext();
            ctx.setOperationType("CREATE");

            result.setData(category);
        }
        catch(Exception ex) {
            result.addErrorMessage(ex.getMessage(), ResultType.INVALID);
        }

        return CompletableFuture.completedFuture(result);
    }

    public CompletableFuture<Result> updateCategory(Long categoryId, CategoryRequestDTO categoryRequestDTO) {
        if(categoryId < 0) {
            throw new InvalidRequestException("Category id cannot be negative.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category id " + categoryId + " does not exist."));

        if(categoryRequestDTO.getName() == null || categoryRequestDTO.getName().isBlank()) {
            throw new InvalidRequestException("Category name cannot be null or empty.");
        }

        if(!categoryRepository.findByName(categoryRequestDTO.getName()).isEmpty()) {
            throw new ResourceConflictException("Category name already exists.");
        }

        AuditContext ctx = AuditContextHolder.getContext();
        ctx.setOperationType("UPDATE");

        category.setName(categoryRequestDTO.getName());

        Category savedCategory = categoryRepository.save(category);

        Result result = new Result();
        result.setData(savedCategory);

        return CompletableFuture.completedFuture(result);
    }

    public boolean deleteCategory(Long categoryId) {
        if(categoryId < 0) {
            throw new InvalidRequestException("Category id cannot be negative.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category id " + categoryId + " not found."));

        AuditContext ctx = AuditContextHolder.getContext();
        ctx.setOperationType("DELETE");

        categoryRepository.delete(category);

        return true;
    }
}
