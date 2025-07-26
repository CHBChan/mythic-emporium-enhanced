package com.mythicemporium.controller;

import com.mythicemporium.dto.ProductRequestDTO;
import com.mythicemporium.dto.ProductResponseDTO;
import com.mythicemporium.dto.ProductVariationAttributeDTO;
import com.mythicemporium.dto.ProductVariationRequestDTO;
import com.mythicemporium.model.Brand;
import com.mythicemporium.model.Category;
import com.mythicemporium.model.Product;
import com.mythicemporium.service.Result;
import com.mythicemporium.service.ProductService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @Test
    @WithMockUser
    void getAllProductsShouldReturnProductList() throws Exception {
        List<ProductResponseDTO> products = List.of(
                generateProductResponse(1L),
                generateProductResponse(2L)
        );
        when(service.findAll()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser
    void getAllProductsByBrandIdShouldReturnFilteredList() throws Exception {
        List<ProductResponseDTO> products = List.of(generateProductResponse(1L));
        when(service.findAllByBrandId(1L)).thenReturn(products);

        mockMvc.perform(get("/api/products/brand/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser
    void getAllProductsByCategoryIdShouldReturnFilteredList() throws Exception {
        List<ProductResponseDTO> products = List.of(generateProductResponse(1L));
        when(service.findAllByCategoryId(1L)).thenReturn(products);

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void createProductAsNonAdminShouldReturn403() throws Exception {
        when(service.createProduct(any(ProductRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getRequestJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createValidProductShouldReturn201() throws Exception {
        when(service.createProduct(any(ProductRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(getRequestJson())
                .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createInvalidProductShouldReturn400() throws Exception {
        when(service.createProduct(any(ProductRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateBadResult()));

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void updateProductAsNonAdminShouldReturn403() throws Exception {
        when(service.updateProduct(any(Long.class), any(ProductRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getRequestJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateValidProductShouldReturn200() throws Exception {
        when(service.updateProduct(any(Long.class), any(ProductRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        MvcResult mvcResult = mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateInvalidProductShouldReturn400() throws Exception {
        when(service.updateProduct(any(Long.class), any(ProductRequestDTO.class))).thenReturn(CompletableFuture.completedFuture(generateBadResult()));

        MvcResult mvcResult = mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void deleteProductAsNonAdminShouldReturn403() throws Exception {
        when(service.deleteProduct(any(Long.class))).thenReturn(true);

        mockMvc.perform(delete("/api/products/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteValidProductShouldReturn204() throws Exception {
        when(service.deleteProduct(any(Long.class))).thenReturn(true);

        mockMvc.perform(delete("/api/products/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteValidProductShouldReturn400() throws Exception {
        when(service.deleteProduct(any(Long.class))).thenReturn(false);

        mockMvc.perform(delete("/api/products/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void createVariationAsNonAdminShouldReturn403() throws Exception {
        when(service.createVariation(any(Long.class), any(ProductVariationRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        mockMvc.perform(post("/api/products/1/variations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getVariationRequestJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createValidVariationShouldReturn201() throws Exception {
        when(service.createVariation(any(Long.class), any(ProductVariationRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        MvcResult mvcResult = mockMvc.perform(post("/api/products/1/variations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getVariationRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createInvalidVariationShouldReturn400() throws Exception {
        when(service.createVariation(any(Long.class), any(ProductVariationRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(generateBadResult()));

        MvcResult mvcResult = mockMvc.perform(post("/api/products/1/variations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getVariationRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void updateVariationAsNonAdminShouldReturn403() throws Exception {
        when(service.updateVariation(any(Long.class), any(ProductVariationRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        mockMvc.perform(put("/api/products/variations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getVariationRequestJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateValidVariationShouldReturn200() throws Exception {
        when(service.updateVariation(any(Long.class), any(ProductVariationRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(generateGoodResult()));

        MvcResult mvcResult = mockMvc.perform(put("/api/products/variations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getVariationRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateInvalidVariationShouldReturn400() throws Exception {
        when(service.updateVariation(any(Long.class), any(ProductVariationRequestDTO.class)))
                .thenReturn(CompletableFuture.completedFuture(generateBadResult()));

        MvcResult mvcResult = mockMvc.perform(put("/api/products/variations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getVariationRequestJson())
                        .with(csrf()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void updateVariationStockAsNonAdminShouldReturn403() throws Exception {
        when(service.updateVariationStock(1L, 50)).thenReturn(true);

        mockMvc.perform(patch("/api/products/variations/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getStockUpdateJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateVariationStockSuccessfullyShouldReturn200() throws Exception {
        when(service.updateVariationStock(1L, 50)).thenReturn(true);

        mockMvc.perform(patch("/api/products/variations/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getStockUpdateJson())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Stock updated for variation 1."));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateVariationStockFailureShouldReturn400() throws Exception {
        when(service.updateVariationStock(1L, 50)).thenReturn(false);

        mockMvc.perform(patch("/api/products/variations/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getStockUpdateJson())
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void updateVariationPriceAsNonAdminShouldReturn403() throws Exception {
        when(service.updateVariationPrice(1L, 29.99)).thenReturn(true);

        mockMvc.perform(patch("/api/products/variations/1/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getPriceUpdateJson())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateVariationPriceSuccessfullyShouldReturn200() throws Exception {
        when(service.updateVariationPrice(1L, 29.99)).thenReturn(true);

        mockMvc.perform(patch("/api/products/variations/1/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getPriceUpdateJson())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Price updated for variation 1."));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateVariationPriceFailureShouldReturn400() throws Exception {
        when(service.updateVariationPrice(1L, 29.99)).thenReturn(false);

        mockMvc.perform(patch("/api/products/variations/1/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getPriceUpdateJson())
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"DEMO", "GUEST", "USER"})
    void deleteVariationAsNonAdminShouldReturn403() throws Exception {
        when(service.deleteVariation(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/variations/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteVariationSuccessfullyShouldReturn204() throws Exception {
        when(service.deleteVariation(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/variations/1")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(content().string("Variation 1 successfully deleted."));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteVariationFailureShouldReturn400() throws Exception {
        when(service.deleteVariation(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/products/variations/1")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // Helper methods

    private String getRequestJson() {
        return """
                {
                    "name": "Test Product",
                    "description": "Test Description",
                    "brandId": 1,
                    "categoryId": 1
                }
                """;
    }

    private Result generateGoodResult() {
        return new Result();
    }

    private Result generateBadResult() {
        Result result = new Result();
        result.addErrorMessage("Bad result.", ResultType.INVALID);
        return result;
    }

    private Product generateProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product " + id);
        product.setDescription("Test Description " + id);
        product.setBrand(createTestBrand());
        product.setCategory(createTestCategory());
        product.setVariations(new ArrayList<>());
        return product;
    }

    private ProductRequestDTO generateProductRequest() {
        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setName("Test Product 1");
        dto.setDescription("Test Description 1");
        dto.setBrandId(1L);
        dto.setCategoryId(1L);
        return dto;
    }

    private ProductVariationRequestDTO generateVariationRequest() {
        ProductVariationRequestDTO dto = new ProductVariationRequestDTO();
        dto.setSku("Test SKU");
        dto.setPrice(1.99);
        dto.setStock(10);
        dto.setImageUrl("Test url");
        return dto;
    }

    private ProductVariationAttributeDTO generateAttributeDTO() {
        ProductVariationAttributeDTO dto = new ProductVariationAttributeDTO();
        dto.setAttributeName("Size");
        dto.setAttributeValue("Default Size");
        return dto;
    }

    private Brand createTestBrand() {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Test Brand");
        return brand;
    }

    private Category createTestCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        return category;
    }

    private ProductResponseDTO generateProductResponse(Long id) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(id);
        dto.setName("Test Product " + id);
        dto.setDescription("Test Description " + id);
        dto.setBrandName("Test Brand");
        dto.setCategoryName("Test Category");
        dto.setVariations(new ArrayList<>());
        return dto;
    }

    private String getVariationRequestJson() {
        return """
            {
                "sku": "TEST-SKU-001",
                "price": 19.99,
                "stock": 100,
                "imageUrl": "https://example.com/image.jpg",
                "attributes": [
                    {
                        "attributeName": "Size",
                        "attributeValue": "Medium"
                    },
                    {
                        "attributeName": "Color",
                        "attributeValue": "Blue"
                    }
                ]
            }
            """;
    }

    private String getStockUpdateJson() {
        return """
            {
                "stock": 50
            }
            """;
    }

    private String getPriceUpdateJson() {
        return """
            {
                "price": 29.99
            }
            """;
    }
}