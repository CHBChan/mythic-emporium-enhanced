package com.mythicemporium.controller;

import com.mythicemporium.dto.CategoryRequestDTO;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
import com.mythicemporium.model.Category;
import com.mythicemporium.service.CategoryService;
import com.mythicemporium.service.Result;
import com.mythicemporium.service.ResultType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService service;

    @Test
    @WithMockUser
    void getAllCategoriesShouldReturnCategoryList() throws Exception {
        List<Category> categories = List.of(
                generateCategory(1L),
                generateCategory(2L)
        );
        when(service.findAll()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser
    void findCategoryByIdShouldReturnCategory() throws Exception {
        Category category = generateCategory(1L);
        when(service.findById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Category 1")));
    }

    @Test
    @WithMockUser
    void findCategoryByIdNotFoundShouldReturn404() throws Exception {
        when(service.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void createCategoryAsNonAdminShouldReturn403() throws Exception {
        when(service.createCategory(any(CategoryRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createValidCategoryShouldReturn201() throws Exception {
        when(service.createCategory(any(CategoryRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        MvcResult mvcResult = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createInvalidCategoryShouldReturn400() throws Exception {
        when(service.createCategory(any(CategoryRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateBadResult()));

        MvcResult mvcResult = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCategoryWithNullNameShouldThrowException() throws Exception {
        when(service.createCategory(any(CategoryRequestDTO.class)))
                .thenThrow(new InvalidRequestException("Category name cannot be null or empty."));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": null
                            }
                            """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCategoryWithDuplicateNameShouldThrowException() throws Exception {
        when(service.createCategory(any(CategoryRequestDTO.class)))
                .thenThrow(new ResourceConflictException("Category name already exists."));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void updateCategoryAsNonAdminShouldReturn403() throws Exception {
        when(service.updateCategory(any(Long.class), any(CategoryRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateValidCategoryShouldReturn200() throws Exception {
        when(service.updateCategory(any(Long.class), any(CategoryRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        MvcResult mvcResult = mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateInvalidCategoryShouldReturn400() throws Exception {
        when(service.updateCategory(any(Long.class), any(CategoryRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateBadResult()));

        MvcResult mvcResult = mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateNonExistentCategoryShouldThrowException() throws Exception {
        when(service.updateCategory(eq(999L), any(CategoryRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category id 999 does not exist."));

        mockMvc.perform(put("/api/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateCategoryWithDuplicateNameShouldThrowException() throws Exception {
        when(service.updateCategory(eq(1L), any(CategoryRequestDTO.class)))
                .thenThrow(new ResourceConflictException("Category name already exists."));

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getCategoryRequestJson())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void deleteCategoryAsNonAdminShouldReturn403() throws Exception {
        when(service.deleteCategory(any(Long.class))).thenReturn(true);

        mockMvc.perform(delete("/api/categories/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategorySuccessfullyShouldReturn204() throws Exception {
        when(service.deleteCategory(any(Long.class))).thenReturn(true);

        mockMvc.perform(delete("/api/categories/1")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string("Category 1 successfully deleted."));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategoryFailureShouldReturn400() throws Exception {
        when(service.deleteCategory(any(Long.class))).thenReturn(false);

        mockMvc.perform(delete("/api/categories/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteNonExistentCategoryShouldThrowException() throws Exception {
        when(service.deleteCategory(eq(999L)))
                .thenThrow(new ResourceNotFoundException("Category 999 not found."));

        mockMvc.perform(delete("/api/categories/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategoryWithNegativeIdShouldThrowException() throws Exception {
        when(service.deleteCategory(eq(-1L)))
                .thenThrow(new InvalidRequestException("Category id cannot be negative."));

        mockMvc.perform(delete("/api/categories/-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // Helper methods

    private String getCategoryRequestJson() {
        return """
                {
                    "name": "Test Category"
                }
                """;
    }

    private Result generateGoodResult() {
        Result result = new Result();
        result.setData(generateCategory(1L));
        return result;
    }

    private Result generateBadResult() {
        Result result = new Result();
        result.addErrorMessage("Bad result.", ResultType.INVALID);
        return result;
    }

    private Category generateCategory(Long id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Test Category " + id);
        return category;
    }

    private CategoryRequestDTO generateCategoryRequest() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Test Category");
        return dto;
    }
}