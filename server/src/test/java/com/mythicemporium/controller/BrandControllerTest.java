package com.mythicemporium.controller;

import com.mythicemporium.dto.BrandRequestDTO;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
import com.mythicemporium.model.Brand;
import com.mythicemporium.service.BrandService;
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


import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandService service;

    @Test
    @WithMockUser
    void getAllBrandsShouldReturnBrandList() throws Exception {
        List<Brand> brands = List.of(
                generateBrand(1L),
                generateBrand(2L)
        );
        when(service.findAll()).thenReturn(brands);

        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser
    void findBrandByIdShouldReturnBrand() throws Exception {
        Brand brand = generateBrand(1L);
        when(service.findById(1L)).thenReturn(brand);

        mockMvc.perform(get("/api/brands/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Brand 1")));
    }

    @Test
    @WithMockUser
    void findBrandByIdNotFoundShouldReturn404() throws Exception {
        when(service.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/brands/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void createBrandAsNonAdminShouldReturn403() throws Exception {
        when(service.createBrand(any(BrandRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createValidBrandShouldReturn201() throws Exception {
        when(service.createBrand(any(BrandRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        MvcResult mvcResult = mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createInvalidBrandShouldReturn400() throws Exception {
        when(service.createBrand(any(BrandRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateBadResult()));

        MvcResult mvcResult = mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createBrandWithNullNameShouldThrowException() throws Exception {
        when(service.createBrand(any(BrandRequestDTO.class)))
                .thenThrow(new InvalidRequestException("Brand name cannot be null or empty."));

        mockMvc.perform(post("/api/brands")
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
    void createBrandWithDuplicateNameShouldThrowException() throws Exception {
        when(service.createBrand(any(BrandRequestDTO.class)))
                .thenThrow(new ResourceConflictException("Brand name already exists."));

        mockMvc.perform(post("/api/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void updateBrandAsNonAdminShouldReturn403() throws Exception {
        when(service.updateBrand(any(Long.class), any(BrandRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        mockMvc.perform(put("/api/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateValidBrandShouldReturn200() throws Exception {
        when(service.updateBrand(any(Long.class), any(BrandRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        MvcResult mvcResult = mockMvc.perform(put("/api/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateInvalidBrandShouldReturn400() throws Exception {
        when(service.updateBrand(any(Long.class), any(BrandRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateBadResult()));

        MvcResult mvcResult = mockMvc.perform(put("/api/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateNonExistentBrandShouldThrowException() throws Exception {
        when(service.updateBrand(eq(999L), any(BrandRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Brand id 999 does not exist."));

        mockMvc.perform(put("/api/brands/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateBrandWithDuplicateNameShouldThrowException() throws Exception {
        when(service.updateBrand(eq(1L), any(BrandRequestDTO.class)))
                .thenThrow(new ResourceConflictException("Brand name already exists."));

        mockMvc.perform(put("/api/brands/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getBrandRequestJson())
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void deleteBrandAsNonAdminShouldReturn403() throws Exception {
        when(service.deleteBrand(any(Long.class))).thenReturn(true);

        mockMvc.perform(delete("/api/brands/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteBrandSuccessfullyShouldReturn204() throws Exception {
        when(service.deleteBrand(any(Long.class))).thenReturn(true);

        mockMvc.perform(delete("/api/brands/1")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string("Brand 1 successfully deleted."));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteBrandFailureShouldReturn400() throws Exception {
        when(service.deleteBrand(any(Long.class))).thenReturn(false);

        mockMvc.perform(delete("/api/brands/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteNonExistentBrandShouldThrowException() throws Exception {
        when(service.deleteBrand(eq(999L)))
                .thenThrow(new ResourceNotFoundException("Brand 999 not found."));

        mockMvc.perform(delete("/api/brands/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteBrandWithNegativeIdShouldThrowException() throws Exception {
        when(service.deleteBrand(eq(-1L)))
                .thenThrow(new InvalidRequestException("Brand id cannot be negative."));

        mockMvc.perform(delete("/api/brands/-1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // Helper methods

    private String getBrandRequestJson() {
        return """
                {
                    "name": "Test Brand"
                }
                """;
    }

    private Result generateGoodResult() {
        Result result = new Result();
        result.setData(generateBrand(1L));
        return result;
    }

    private Result generateBadResult() {
        Result result = new Result();
        result.addErrorMessage("Bad result.", ResultType.INVALID);
        return result;
    }

    private Brand generateBrand(Long id) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName("Test Brand " + id);
        return brand;
    }

    private BrandRequestDTO generateBrandRequest() {
        BrandRequestDTO dto = new BrandRequestDTO();
        dto.setName("Test Brand");
        return dto;
    }
}