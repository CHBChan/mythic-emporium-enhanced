package com.mythicemporium.service;

import com.mythicemporium.dto.*;
import com.mythicemporium.exception.InsufficientStockException;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
import com.mythicemporium.logging.AuditContext;
import com.mythicemporium.logging.AuditContextHolder;
import com.mythicemporium.model.*;
import com.mythicemporium.repository.BrandRepository;
import com.mythicemporium.repository.CategoryRepository;
import com.mythicemporium.repository.ProductRepository;
import com.mythicemporium.repository.ProductVariationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    private ProductRepository productRepository;
    private ProductVariationRepository productVariationRepository;
    private BrandRepository brandRepository;
    private CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductVariationRepository productVariationRepository, BrandRepository brandRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productVariationRepository = productVariationRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    public List<ProductResponseDTO> findAllByBrandId(Long id) {
        return productRepository.findAllByBrandId(id).stream().map(this::toResponseDTO).toList();
    }

    public List<ProductResponseDTO> findAllByCategoryId(Long id) {
        return productRepository.findAllByCategoryId(id).stream().map(this::toResponseDTO).toList();
    }

    public ProductResponseDTO findById(Long id) {
        Optional<Product> product = productRepository.findById(id);

        return product.map(this::toResponseDTO).orElse(null);
    }

    public CompletableFuture<Result> createProduct(ProductRequestDTO productRequest) {
        validateProductRequestDTO((long) 0, productRequest);

        Brand brand = brandRepository.findById(productRequest.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Product brand not found."));
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Product category not found."));

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setBrand(brand);
        product.setCategory(category);

        if(productRequest.getVariations() != null) {
            for(ProductVariationRequestDTO variationDTO : productRequest.getVariations()) {
                ProductVariation variation = new ProductVariation();
                variation.setSku(variationDTO.getSku());
                variation.setPrice(variationDTO.getPrice());
                variation.setStock(variationDTO.getStock());
                variation.setImageUrl(variationDTO.getImageUrl());
                variation.setProduct(product);

                if(variationDTO.getAttributes() != null) {
                    for(ProductVariationAttributeDTO attributeDTO : variationDTO.getAttributes()) {
                        ProductVariationAttribute attribute = new ProductVariationAttribute();
                        attribute.setAttributeName(attributeDTO.getAttributeName());
                        attribute.setAttributeValue(attributeDTO.getAttributeValue());
                        attribute.setVariation(variation);
                        variation.getAttributes().add(attribute);
                    }
                }
                else {
                    throw new InvalidRequestException("Product variation attribute cannot be null.");
                }

                product.getVariations().add(variation);
            }
        }
        else {
            throw new InvalidRequestException("Product variation cannot be null.");
        }

        Result result = new Result();

        try {
            product = productRepository.save(product);

            AuditContext ctx = AuditContextHolder.getContext();
            ctx.setOperationType("CREATE");

            result.setData(toResponseDTO(product));
        }
        catch(Exception ex) {
            result.addErrorMessage(ex.getMessage(), ResultType.INVALID);
        }

        return CompletableFuture.completedFuture(result);
    }

    public CompletableFuture<Result> updateProduct(Long productId, ProductRequestDTO productRequest) {
        validateProductRequestDTO(productId, productRequest);

        if(productRequest.getVariations() != null) {
            throw new InvalidRequestException("Product variations cannot be set for updating.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not found."));

        Brand brand = getBrandIfChanged(product, productRequest.getBrandId());
        Category category = getCategoryIfChanged(product, productRequest.getCategoryId());

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setBrand(brand);
        product.setCategory(category);

        AuditContext ctx = AuditContextHolder.getContext();
        ctx.setOperationType("UPDATE");

        Result result = new Result();
        Product savedProduct = productRepository.save(product);
        result.setData(toResponseDTO(savedProduct));

        return CompletableFuture.completedFuture(result);
    }

    public boolean deleteProduct(Long productId) {
        if(productId < 0) {
            throw new InvalidRequestException("Product id cannot be negative.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product id " + productId + " not found."));

        AuditContext ctx = AuditContextHolder.getContext();
        ctx.setOperationType("DELETE");

        productRepository.delete(product);

        return true;
    }

    public CompletableFuture<Result> createVariation(Long productId, ProductVariationRequestDTO productVariationDTO) {
        validateProductVariationRequestDTO(productId, productVariationDTO);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product id " + productId + " not found."));

        if(product.getVariations().stream().anyMatch(v -> v.getSku().equalsIgnoreCase(productVariationDTO.getSku()))) {
            throw new ResourceConflictException("Product variation SKU " + productVariationDTO.getSku() + " already exists in database.");
        }

        ProductVariation variation = new ProductVariation();
        variation.setSku(productVariationDTO.getSku());
        variation.setPrice(productVariationDTO.getPrice());
        variation.setStock(productVariationDTO.getStock());
        variation.setImageUrl(productVariationDTO.getImageUrl());
        variation.setProduct(product);

        List<ProductVariationAttribute> attributes = productVariationDTO.getAttributes().stream()
                .map(attrDto -> {
                    ProductVariationAttribute attr = new ProductVariationAttribute();
                    attr.setAttributeName(attrDto.getAttributeName());
                    attr.setAttributeValue(attrDto.getAttributeValue());
                    attr.setVariation(variation);
                    return attr;
                })
                .collect(Collectors.toList());

        variation.setAttributes(attributes);
        productVariationRepository.save(variation);
        product.getVariations().add(variation);

        AuditContext ctx = AuditContextHolder.getContext();
        ctx.setOperationType("CREATE");

        Result result = new Result();
        result.setData(toResponseDTO(product));

        return CompletableFuture.completedFuture(result);
    }

    public CompletableFuture<Result> updateVariation(Long variationId, ProductVariationRequestDTO productVariationDTO) {
        if(variationId < 0) {
            throw new InvalidRequestException("Variation id cannot be negative.");
        }

        ProductVariation variation = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Variation id " + variationId + " not found."));

        Product product = variation.getProduct();
        if(product == null) {
            throw new ResourceNotFoundException("Product for variation id " + variationId + " not found.");
        }

        if(product.getVariations().stream().anyMatch(v -> v.getSku().equalsIgnoreCase(productVariationDTO.getSku()) && !Objects.equals(v.getId(), variationId))) {
            throw new ResourceConflictException("Product variation SKU " + productVariationDTO.getSku() + " already exists in database.");
        }

        variation.setSku(productVariationDTO.getSku());
        variation.setPrice(productVariationDTO.getPrice());
        variation.setStock(productVariationDTO.getStock());
        variation.setImageUrl(productVariationDTO.getImageUrl());

        variation.getAttributes().clear();

        List<ProductVariationAttribute> attributes = productVariationDTO.getAttributes().stream()
                .map(attrDto -> {
                    ProductVariationAttribute attr = new ProductVariationAttribute();
                    attr.setAttributeName(attrDto.getAttributeName());
                    attr.setAttributeValue(attrDto.getAttributeValue());
                    attr.setVariation(variation);
                    return attr;
                })
                .collect(Collectors.toList());

        variation.getAttributes().addAll(attributes);

        productVariationRepository.save(variation);

        AuditContext ctx = AuditContextHolder.getContext();
        ctx.setOperationType("UPDATE");

        Result result = new Result();
        result.setData(toResponseDTO(product));

        return CompletableFuture.completedFuture(result);
    }

    @Transactional
    public boolean purchaseProduct(Long variationId, Integer quantity) {
        if (variationId < 0) {
            throw new InvalidRequestException("Variation id cannot be negative.");
        }

        if (quantity == null || quantity <= 0) {
            throw new InvalidRequestException("Quantity must be positive and non-null.");
        }

        int updated = productVariationRepository.decrementStock(variationId, quantity);
        if (updated == 0) {
            throw new InsufficientStockException("Not enough stock available or product not found.");
        }
        return true;
    }

    @Transactional
    public boolean updateVariationPrice(Long variationId, Double price) {
        if(variationId < 0) {
            throw new InvalidRequestException("Variation id cannot be negative.");
        }

        if(price == null || price < 0) {
            throw new InvalidRequestException("Price must be positive and non-null.");
        }

        int updated = productVariationRepository.updatePriceById(variationId, price);
        if(updated == 0) {
            throw new ResourceNotFoundException("Variation " + variationId + " not found.");
        }
        return true;
    }

    @Transactional
    public boolean updateVariationStock(Long variationId, Integer stock) {
        if(variationId < 0) {
            throw new InvalidRequestException("Variation id cannot be negative.");
        }

        if(stock == null || stock < 0) {
            throw new InvalidRequestException("Stock must be positive and non-null.");
        }

        int updated = productVariationRepository.updateStockById(variationId, stock);
        if (updated == 0) {
            throw new ResourceNotFoundException("Variation " + variationId + " not found.");
        }
        return true;
    }

    @Transactional
    public boolean deleteVariation(Long variationId) {
        if(variationId < 0) {
            throw new InvalidRequestException("Variation id cannot be negative.");
        }

        ProductVariation pv = productVariationRepository.findById(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Variation id " + variationId + " not found."));

        AuditContext ctx = AuditContextHolder.getContext();
        ctx.setOperationType("DELETE");

        productVariationRepository.delete(pv);

        return true;
    }

    private void validateProductRequestDTO(Long productId, ProductRequestDTO productRequestDTO) {
        if(productId < 0) {
            throw new InvalidRequestException("Product id cannot be negative.");
        }

        if(productRequestDTO == null) {
            throw new InvalidRequestException("Product information must not be null");
        }

        if(productRequestDTO.getName() == null || productRequestDTO.getName().isBlank()) {
            throw new InvalidRequestException("Product name cannot be empty or null.");
        }

        if(productRequestDTO.getDescription() == null || productRequestDTO.getDescription().isBlank()) {
            throw new InvalidRequestException("Product description cannot be empty or null.");
        }

        if(productRequestDTO.getBrandId() == null || productRequestDTO.getBrandId() < 0) {
            throw new InvalidRequestException("Product brand id cannot be null or negative.");
        }

        if(productRequestDTO.getCategoryId() == null || productRequestDTO.getCategoryId() < 0) {
            throw new InvalidRequestException("Product category id cannot be null or negative.");
        }
    }

    private void validateProductVariationRequestDTO(Long variationId, ProductVariationRequestDTO productVariationRequestDTO) {
        if(variationId < 0) {
            throw new InvalidRequestException("Product id cannot be negative.");
        }

        if(productVariationRequestDTO == null) {
            throw new InvalidRequestException("Variation information cannot be null.");
        }

        if(productVariationRequestDTO.getSku() == null || productVariationRequestDTO.getSku().isBlank()) {
            throw new InvalidRequestException("Variation SKU cannot be null or empty.");
        }

        if(productVariationRequestDTO.getPrice() < 0) {
            throw new InvalidRequestException("Variation price cannot be less than 0.");
        }

        if(productVariationRequestDTO.getStock() < 0) {
            throw new InvalidRequestException("Variation stock cannot be less than 0.");
        }

        if(productVariationRequestDTO.getAttributes() == null) {
            throw new InvalidRequestException("Variation attribute cannot be null.");
        }
    }

    private Brand getBrandIfChanged(Product existingProduct, Long newBrandId) {
        if(existingProduct.getBrand() != null &&
           existingProduct.getBrand().getId().equals(newBrandId)) {
            return existingProduct.getBrand();
        }

        return brandRepository.findById(newBrandId)
                .orElseThrow(() -> new ResourceNotFoundException("Product brand " + newBrandId + " not found."));
    }

    private Category getCategoryIfChanged(Product existingProduct, Long newCategoryId) {
        if (existingProduct.getCategory() != null &&
            existingProduct.getCategory().getId().equals(newCategoryId)) {
            return existingProduct.getCategory();
        }

        return categoryRepository.findById(newCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Product category " + newCategoryId + " not found."));
    }

    private ProductResponseDTO toResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBrandName(product.getBrand().getName());
        dto.setCategoryName(product.getCategory().getName());

        List<ProductVariationResponseDTO> variationDTOs = product.getVariations().stream().map(variation -> {
            ProductVariationResponseDTO varDto = new ProductVariationResponseDTO();
            varDto.setId(variation.getId());
            varDto.setSku(variation.getSku());
            varDto.setPrice(variation.getPrice());
            varDto.setStock(variation.getStock());
            varDto.setImageUrl(variation.getImageUrl());

            List<ProductVariationAttributeDTO> attrDTOs = variation.getAttributes().stream().map(attr -> {
                ProductVariationAttributeDTO attrDto = new ProductVariationAttributeDTO();
                attrDto.setAttributeName(attr.getAttributeName());
                attrDto.setAttributeValue(attr.getAttributeValue());
                return attrDto;
            }).collect(Collectors.toList());

            varDto.setAttributes(attrDTOs);
            return varDto;
        }).collect(Collectors.toList());

        dto.setVariations(variationDTOs);
        return dto;
    }
}