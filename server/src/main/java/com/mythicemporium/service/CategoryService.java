package com.mythicemporium.service;

import com.mythicemporium.dto.CategoryRequestDTO;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
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

        if(categoryRepository.findById(categoryId).isEmpty()) {
            throw new ResourceNotFoundException("Category id " + categoryId + " does not exist.");
        }

        if(categoryRequestDTO.getName() == null || categoryRequestDTO.getName().isBlank()) {
            throw new InvalidRequestException("Category name cannot be null or empty.");
        }

        if(!categoryRepository.findByName(categoryRequestDTO.getName()).isEmpty()) {
            throw new ResourceConflictException("Category name already exists.");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryRequestDTO.getName());

        Result result = new Result();
        result.setData(category);

        return CompletableFuture.completedFuture(result);
    }

    public boolean deleteCategory(Long categoryId) {
        if(categoryId < 0) {
            throw new InvalidRequestException("Category id cannot be negative.");
        }

        if(categoryRepository.deleteByIdAndReturnCount(categoryId) == 0) {
            throw new ResourceNotFoundException("Category " + categoryId + " not found.");
        }
        return true;
    }
}
