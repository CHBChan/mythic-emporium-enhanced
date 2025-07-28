package com.mythicemporium.service;

import com.mythicemporium.dto.*;
import com.mythicemporium.exception.InsufficientStockException;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
import com.mythicemporium.model.*;
import com.mythicemporium.repository.BrandRepository;
import com.mythicemporium.repository.CategoryRepository;
import com.mythicemporium.repository.ProductRepository;
import com.mythicemporium.repository.ProductVariationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    BrandRepository brandRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductVariationRepository productVariationRepository;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(productRepository, productVariationRepository, brandRepository, categoryRepository);
    }

    @Test
    void shouldFindAll() {
        when(productRepository.findAll()).thenReturn(List.of(generateProduct(1L)));
        List<ProductResponseDTO> products = service.findAll();

        assertEquals(1, products.size());
    }

    @Test
    void findAllByBrandId() {
        when(productRepository.findAllByBrandId(any(Long.class))).thenReturn(List.of(generateProduct(1L)));
        List<ProductResponseDTO> products = service.findAllByBrandId(1L);

        assertEquals(1, products.size());
    }

    @Test
    void findAllByCategoryId() {
        when(productRepository.findAllByCategoryId(any(Long.class))).thenReturn(List.of(generateProduct(1L)));
        List<ProductResponseDTO> products = service.findAllByCategoryId(1L);

        assertEquals(1, products.size());
    }

    @Test
    void shouldCreateValidProduct() throws ExecutionException, InterruptedException {
        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.of(createTestBrand()));
        when(categoryRepository.findById(any(Long.class))).thenReturn(Optional.of(createTestCategory()));
        when(productRepository.save(any(Product.class))).thenReturn(generateProduct(1L));

        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        ProductRequestDTO productRequestDTO = generateProductRequest();
        productRequestDTO.setVariations(List.of(variationRequestDTO));

        CompletableFuture<Result> result = service.createProduct(productRequestDTO);

        assertTrue(result.isDone());
        assertNotNull(result.get());
        assertTrue(result.get().isSuccess());

        assertEquals(productRequestDTO.getName(), ((ProductResponseDTO) result.get().getData()).getName());
    }

    @Test
    void shouldNotCreateProductWithInvalidDTO() {
        assertThrows(InvalidRequestException.class, () -> service.createProduct(null));
    }

    @Test
    void shouldNotCreateProductWithInvalidName() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        ProductRequestDTO productRequestDTO = generateProductRequest();
        productRequestDTO.setVariations(List.of(variationRequestDTO));

        // Test null name
        productRequestDTO.setName(null);
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));

        // Test empty name
        productRequestDTO.setName("");
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));
    }

    @Test
    void shouldNotCreateProductWithInvalidDescription() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        ProductRequestDTO productRequestDTO = generateProductRequest();
        productRequestDTO.setVariations(List.of(variationRequestDTO));

        // Test null description
        productRequestDTO.setDescription(null);
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));

        // Test empty description
        productRequestDTO.setDescription("");
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));
    }

    @Test
    void shouldNotCreateProductWithInvalidBrand() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        ProductRequestDTO productRequestDTO = generateProductRequest();
        productRequestDTO.setVariations(List.of(variationRequestDTO));

        // Test null brand id
        productRequestDTO.setBrandId(null);
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));

        // Test negative brand id
        productRequestDTO.setBrandId(-1L);
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));
    }

    @Test
    void shouldNotCreateProductWithNonExistentBrand() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        ProductRequestDTO productRequestDTO = generateProductRequest();
        productRequestDTO.setVariations(List.of(variationRequestDTO));

        productRequestDTO.setBrandId(2L);
        assertThrows(ResourceNotFoundException.class, () -> service.createProduct(productRequestDTO));
    }

    @Test
    void shouldNotCreateProductWithInvalidCategory() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        ProductRequestDTO productRequestDTO = generateProductRequest();
        productRequestDTO.setVariations(List.of(variationRequestDTO));

        // Test null category id
        productRequestDTO.setCategoryId(null);
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));

        // Test negative category id
        productRequestDTO.setCategoryId(-1L);
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));
    }

    @Test
    void shouldNotCreateProductWithNonExistentCategory() {
        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.of(createTestBrand()));
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        ProductRequestDTO productRequestDTO = generateProductRequest();
        productRequestDTO.setVariations(List.of(variationRequestDTO));

        productRequestDTO.setCategoryId(2L);
        assertThrows(ResourceNotFoundException.class, () -> service.createProduct(productRequestDTO));
    }

    @Test
    void shouldUpdateValidProduct() throws ExecutionException, InterruptedException {
        when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(generateProduct(1L)));
        when(productRepository.save(any(Product.class))).thenReturn(generateProduct(1L));

        ProductRequestDTO productRequestDTO = generateProductRequest();

        CompletableFuture<Result> result = service.updateProduct(1L, productRequestDTO);

        assertTrue(result.isDone());
        assertNotNull(result.get());
        assertTrue(result.get().isSuccess());
        assertEquals(productRequestDTO.getName(), ((ProductResponseDTO) result.get().getData()).getName());
    }

    @Test
    void shouldNotUpdateProductWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.updateVariation(-1L, generateVariationRequest()));
    }

    @Test
    void shouldNotUpdateProductWithInvalidDTO() {
        assertThrows(InvalidRequestException.class, () -> service.updateProduct(1L, null));
    }

    @Test
    void shouldNotUpdateProductWithInvalidName() {
        ProductRequestDTO productRequestDTO = generateProductRequest();

        // Test null name
        productRequestDTO.setName(null);
        assertThrows(InvalidRequestException.class, () -> service.updateProduct(1L, productRequestDTO));

        // Test empty name
        productRequestDTO.setName("");
        assertThrows(InvalidRequestException.class, () -> service.updateProduct(1L, productRequestDTO));
    }

    @Test
    void shouldNotUpdateProductWithInvalidDescription() {
        ProductRequestDTO productRequestDTO = generateProductRequest();

        // Test null description
        productRequestDTO.setDescription(null);
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));

        // Test empty description
        productRequestDTO.setDescription("");
        assertThrows(InvalidRequestException.class, () -> service.createProduct(productRequestDTO));
    }

    @Test
    void shouldNotUpdateProductWithVariation() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        ProductRequestDTO productRequestDTO = generateProductRequest();
        productRequestDTO.setVariations(List.of(variationRequestDTO));

        assertThrows(InvalidRequestException.class, () -> service.updateProduct(1L, productRequestDTO));
    }

    @Test
    void shouldNotUpdateProductWithInvalidBrand() {
        ProductRequestDTO productRequestDTO = generateProductRequest();

        // Test null brand id
        productRequestDTO.setBrandId(null);
        assertThrows(InvalidRequestException.class, () -> service.updateProduct(1L, productRequestDTO));

        // Test negative brand id
        productRequestDTO.setBrandId(-1L);
        assertThrows(InvalidRequestException.class, () -> service.updateProduct(1L, productRequestDTO));
    }

    @Test
    void shouldNotUpdateProductWithNonExistentBrand() {
        when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(generateProduct(1L)));
        ProductRequestDTO productRequestDTO = generateProductRequest();

        productRequestDTO.setBrandId(2L);
        assertThrows(ResourceNotFoundException.class, () -> service.updateProduct(1L, productRequestDTO));
    }

    @Test
    void shouldNotUpdateProductWithInvalidCategory() {
        ProductRequestDTO productRequestDTO = generateProductRequest();

        // Test null category id
        productRequestDTO.setCategoryId(null);
        assertThrows(InvalidRequestException.class, () -> service.updateProduct(1L, productRequestDTO));

        // Test negative category id
        productRequestDTO.setCategoryId(-1L);
        assertThrows(InvalidRequestException.class, () -> service.updateProduct(1L, productRequestDTO));
    }

    @Test
    void shouldNotUpdateProductWithNonExistentCategory() {
        when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(generateProduct(1L)));
        ProductRequestDTO productRequestDTO = generateProductRequest();

        productRequestDTO.setCategoryId(2L);
        assertThrows(ResourceNotFoundException.class, () -> service.updateProduct(1L, productRequestDTO));
    }

    @Test
    void shouldDeleteValidProduct() {
        when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(generateProduct(1L)));

        assertTrue(service.deleteProduct(1L));
    }

    @Test
    void shouldNotDeleteProductWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.deleteProduct(-1L));
    }

    @Test
    void shouldNotDeleteNonExistentProduct() {
        assertThrows(ResourceNotFoundException.class, () -> service.deleteVariation(1L));
    }

    @Test
    void shouldCreateValidVariation() throws ExecutionException, InterruptedException {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));

        when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(generateProduct(1L)));

        CompletableFuture<Result> result = service.createVariation(1L, variationRequestDTO);

        assertTrue(result.isDone());
        assertNotNull(result.get());
        assertTrue(result.get().isSuccess());
        assertEquals(variationRequestDTO.getSku(), ((ProductResponseDTO) result.get().getData()).getVariations().get(0).getSku());
    }

    @Test
    void shouldNotCreateVariationWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.createVariation(-1L, generateVariationRequest()));
    }

    @Test
    void shouldNotCreateVariationWithInvalidDTO() {
        assertThrows(InvalidRequestException.class, () -> service.createVariation(1L, null));
    }

    @Test
    void shouldNotCreateVariationWithInvalidSku() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));

        // Test null SKU
        variationRequestDTO.setSku(null);
        assertThrows(InvalidRequestException.class, () -> service.createVariation(1L, variationRequestDTO));

        // Test empty SKU
        variationRequestDTO.setSku("");
        assertThrows(InvalidRequestException.class, () -> service.createVariation(1L, variationRequestDTO));

        // Test blank SKU
        variationRequestDTO.setSku("   ");
        assertThrows(InvalidRequestException.class, () -> service.createVariation(1L, variationRequestDTO));
    }

    @Test
    void shouldNotCreateVariationWithInvalidPrice() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));

        // Test negative price
        variationRequestDTO.setPrice(-1.0);
        assertThrows(InvalidRequestException.class, () -> service.createVariation(1L, variationRequestDTO));
    }

    @Test
    void shouldNotCreateVariationWithInvalidStock() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));

        // Test negative stock
        variationRequestDTO.setStock(-1);
        assertThrows(InvalidRequestException.class, () -> service.createVariation(1L, variationRequestDTO));
    }

    @Test
    void shouldNotCreateVariationWithNullAttributes() {
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(null);

        assertThrows(InvalidRequestException.class, () -> service.createVariation(1L, variationRequestDTO));
    }

    @Test
    void shouldNotCreateVariationWithNonExistentProduct() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));

        when(productRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createVariation(1L, variationRequestDTO));
    }

    @Test
    void shouldNotCreateVariationWithDuplicateSku() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        variationRequestDTO.setSku("EXISTING_SKU");

        Product productWithExistingVariation = generateProduct(1L);
        ProductVariation existingVariation = new ProductVariation();
        existingVariation.setSku("EXISTING_SKU");
        productWithExistingVariation.getVariations().add(existingVariation);

        when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(productWithExistingVariation));

        assertThrows(ResourceConflictException.class, () -> service.createVariation(1L, variationRequestDTO));
    }

    @Test
    void shouldUpdateValidVariation() throws ExecutionException, InterruptedException {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        variationRequestDTO.setSku("UPDATED_SKU");

        Product product = generateProduct(1L);

        ProductVariation existingVariation = new ProductVariation();
        existingVariation.setId(1L);
        existingVariation.setSku("OLD_SKU");
        existingVariation.setPrice(5.99);
        existingVariation.setStock(5);
        existingVariation.setImageUrl("old_url");
        existingVariation.setProduct(product);
        existingVariation.setAttributes(new ArrayList<>());

        product.getVariations().add(existingVariation);

        when(productVariationRepository.findById(any(Long.class))).thenReturn(Optional.of(existingVariation));

        CompletableFuture<Result> result = service.updateVariation(1L, variationRequestDTO);

        assertTrue(result.isDone());
        assertNotNull(result.get());
        assertTrue(result.get().isSuccess());
        assertEquals(variationRequestDTO.getSku(), ((ProductResponseDTO) result.get().getData()).getVariations().get(0).getSku());
    }

    @Test
    void shouldNotUpdateVariationWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.updateVariation(-1L, generateVariationRequest()));
    }

    @Test
    void shouldNotUpdateVariationWithNonExistentVariation() {
        when(productVariationRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateVariation(1L, generateVariationRequest()));
    }

    @Test
    void shouldNotUpdateVariationWithDuplicateSku() {
        ProductVariationAttributeDTO attributeDTO = generateAttributeDTO();
        ProductVariationRequestDTO variationRequestDTO = generateVariationRequest();
        variationRequestDTO.setAttributes(List.of(attributeDTO));
        variationRequestDTO.setSku("DUPLICATE_SKU");

        Product product = generateProduct(1L);

        ProductVariation existingVariation1 = new ProductVariation();
        existingVariation1.setId(1L);
        existingVariation1.setSku("OLD_SKU");
        existingVariation1.setProduct(product);
        existingVariation1.setAttributes(new ArrayList<>());

        ProductVariation existingVariation2 = new ProductVariation();
        existingVariation2.setId(2L);
        existingVariation2.setSku("DUPLICATE_SKU");
        existingVariation2.setProduct(product);

        product.getVariations().add(existingVariation1);
        product.getVariations().add(existingVariation2);

        when(productVariationRepository.findById(1L)).thenReturn(Optional.of(existingVariation1));

        assertThrows(ResourceConflictException.class, () -> service.updateVariation(1L, variationRequestDTO));
    }

    @Test
    void shouldPurchaseValidProduct() {
        when(productVariationRepository.decrementStock(any(Long.class), any(Integer.class))).thenReturn(1);

        assertTrue(service.purchaseProduct(1L, 2));
    }

    @Test
    void shouldNotPurchaseProductWithInvalidVariationId() {
        assertThrows(InvalidRequestException.class, () -> service.purchaseProduct(-1L, 2));
    }

    @Test
    void shouldNotPurchaseProductWithInvalidQuantity() {
        // Test null quantity
        assertThrows(InvalidRequestException.class, () -> service.purchaseProduct(1L, null));

        // Test zero quantity
        assertThrows(InvalidRequestException.class, () -> service.purchaseProduct(1L, 0));

        // Test negative quantity
        assertThrows(InvalidRequestException.class, () -> service.purchaseProduct(1L, -1));
    }

    @Test
    void shouldNotPurchaseProductWithInsufficientStock() {
        when(productVariationRepository.decrementStock(any(Long.class), any(Integer.class))).thenReturn(0);

        assertThrows(InsufficientStockException.class, () -> service.purchaseProduct(1L, 5));
    }

    @Test
    void shouldUpdateVariationPrice() {
        when(productVariationRepository.updatePriceById(any(Long.class), any(Double.class))).thenReturn(1);

        assertTrue(service.updateVariationPrice(1L, 15.99));
    }

    @Test
    void shouldNotUpdateVariationPriceWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.updateVariationPrice(-1L, 15.99));
    }

    @Test
    void shouldNotUpdateVariationPriceWithInvalidPrice() {
        // Test null price
        assertThrows(InvalidRequestException.class, () -> service.updateVariationPrice(1L, null));

        // Test negative price
        assertThrows(InvalidRequestException.class, () -> service.updateVariationPrice(1L, -1.0));
    }

    @Test
    void shouldNotUpdateVariationPriceWithNonExistentVariation() {
        when(productVariationRepository.updatePriceById(any(Long.class), any(Double.class))).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.updateVariationPrice(1L, 15.99));
    }

    @Test
    void shouldUpdateVariationStock() {
        when(productVariationRepository.updateStockById(any(Long.class), any(Integer.class))).thenReturn(1);

        assertTrue(service.updateVariationStock(1L, 25));
    }

    @Test
    void shouldNotUpdateVariationStockWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.updateVariationStock(-1L, 25));
    }

    @Test
    void shouldNotUpdateVariationStockWithInvalidStock() {
        // Test null stock
        assertThrows(InvalidRequestException.class, () -> service.updateVariationStock(1L, null));

        // Test negative stock
        assertThrows(InvalidRequestException.class, () -> service.updateVariationStock(1L, -1));
    }

    @Test
    void shouldNotUpdateVariationStockWithNonExistentVariation() {
        when(productVariationRepository.updateStockById(any(Long.class), any(Integer.class))).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.updateVariationStock(1L, 25));
    }

    @Test
    void shouldDeleteValidVariation() {
        when(productVariationRepository.findById(any(Long.class))).thenReturn(Optional.of(generateProductVariation(1L)));

        assertTrue(service.deleteVariation(1L));
    }

    @Test
    void shouldNotDeleteVariationWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.deleteVariation(-1L));
    }

    @Test
    void shouldNotDeleteNonExistentVariation() {
        when(productVariationRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteVariation(1L));
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

    private ProductVariation generateProductVariation(Long id) {
        ProductVariation pv = new ProductVariation();
        pv.setId(id);
        pv.setProduct(null);
        pv.setSku("Test SKU");
        pv.setPrice(1.99);
        pv.setImageUrl(null);
        pv.setAttributes(List.of());
        return pv;
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
}