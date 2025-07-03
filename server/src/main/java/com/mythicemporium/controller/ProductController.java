package com.mythicemporium.controller;

import com.mythicemporium.dto.*;
import com.mythicemporium.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Fetches all products")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all products")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Fetches all products filtered by brand")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved brand filtered products")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @GetMapping("/brand/{brandId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDTO>> getAllByBrandId(@PathVariable Long brandId) {
        return ResponseEntity.ok(service.findAllByBrandId(brandId));
    }

    @Operation(summary = "Fetches all products filtered by category ")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category filtered products")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ProductResponseDTO>> getAllByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(service.findAllByCategoryId(categoryId));
    }

    @Operation(summary = "Adds a product and returns it")
    @ApiResponse(responseCode = "201", description = "Successfully added product")
    @ApiResponse(responseCode = "400", description = "Failed to add product")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PostMapping
    @PreAuthorize("hasPermission(#productRequestDTO, 'create')")
    public CompletableFuture<ResponseEntity<?>> createProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        return service.createProduct(productRequestDTO)
                .thenApply(result -> {
                    if(!result.isSuccess()) {
                        return new ResponseEntity<>(result.getErrorMessages(), HttpStatus.BAD_REQUEST);
                    }
                    return new ResponseEntity<>(result.getProductResponse(), HttpStatus.CREATED);
                });
    }

    @Operation(summary = "Updates a product")
    @ApiResponse(responseCode = "200", description = "Successfully updated product")
    @ApiResponse(responseCode = "400", description = "Failed to update product")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PutMapping("/{productId}")
    @PreAuthorize("hasPermission(#productRequestDTO, 'update')")
    public CompletableFuture<ResponseEntity<?>> updateProduct(@PathVariable Long productId, @RequestBody ProductRequestDTO productRequestDTO) {
        return service.updateProduct(productId, productRequestDTO)
                .thenApply(result -> {
                    if(!result.isSuccess()) {
                        return new ResponseEntity<>(result.getErrorMessages(), HttpStatus.BAD_REQUEST);
                    }
                    return ResponseEntity.ok(result.getProductResponse());
                });
    }

    @Operation(summary = "Deletes a product")
    @ApiResponse(responseCode = "204", description = "Successfully deleted product")
    @ApiResponse(responseCode = "400", description = "Failed to delete product")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasPermission(#productId, 'Product', 'delete')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        if(service.deleteProduct(productId)) {
            return new ResponseEntity<>("Product " + productId + " successfully deleted.", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Adds a product variation and returns it")
    @ApiResponse(responseCode = "201", description = "Successfully added product variation")
    @ApiResponse(responseCode = "400", description = "Failed to add product variation")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PostMapping("/{productId}/variations")
    @PreAuthorize("hasPermission(#productVariationDTO, 'create')")
    public CompletableFuture<ResponseEntity<?>> createVariation(@PathVariable Long productId, @RequestBody ProductVariationRequestDTO productVariationDTO) {
        return service.createVariation(productId, productVariationDTO)
                .thenApply(result -> {
                    if(!result.isSuccess()) {
                        return new ResponseEntity<>(result.getErrorMessages(), HttpStatus.BAD_REQUEST);
                    }
                    return new ResponseEntity<>(result.getProductResponse(), HttpStatus.CREATED);
                });
    }

    @Operation(summary = "Updates a product variation")
    @ApiResponse(responseCode = "200", description = "Successfully updated product variation")
    @ApiResponse(responseCode = "400", description = "Failed to update product variation")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PutMapping("/variations/{variationId}")
    @PreAuthorize("hasPermission(#productVariationDTO, 'update')")
    public CompletableFuture<ResponseEntity<?>> updateVariation(@PathVariable Long variationId, @RequestBody ProductVariationRequestDTO productVariationDTO) {
        return service.updateVariation(variationId, productVariationDTO)
                .thenApply(result -> {
                    if(!result.isSuccess()) {
                        return new ResponseEntity<>(result.getErrorMessages(), HttpStatus.BAD_REQUEST);
                    }
                    return new ResponseEntity<>(result.getProductResponse(), HttpStatus.OK);
                });
    }

    @Operation(summary = "Updates a product variation stock")
    @ApiResponse(responseCode = "200", description = "Successfully updated product variation stock")
    @ApiResponse(responseCode = "400", description = "Failed to update product variation stock")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PatchMapping("/variations/{variationId}/stock")
    @PreAuthorize("hasPermission(#stockUpdateDTO, 'update')")
    public ResponseEntity<?> updateVariationStock(@PathVariable Long variationId, @RequestBody StockUpdateDTO stockUpdateDTO) {
        if(service.updateVariationStock(variationId, stockUpdateDTO.getStock())) {
            return ResponseEntity.ok("Stock updated for variation " + variationId + ".");
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Updates a product variation price")
    @ApiResponse(responseCode = "200", description = "Successfully updated product variation price")
    @ApiResponse(responseCode = "400", description = "Failed to update product variation price")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PatchMapping("/variations/{variationId}/price")
    @PreAuthorize("hasPermission(#priceUpdateDTO, 'update')")
    public ResponseEntity<?> updateVariationPrice(@PathVariable Long variationId, @RequestBody PriceUpdateDTO priceUpdateDTO) {
        if(service.updateVariationPrice(variationId, priceUpdateDTO.getPrice())) {
            return ResponseEntity.ok("Price updated for variation " + variationId + ".");
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Operation(summary = "Deletes a product variation")
    @ApiResponse(responseCode = "204", description = "Successfully deleted product variation")
    @ApiResponse(responseCode = "400", description = "Failed to delete product variation")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @DeleteMapping("/variations/{variationId}")
    @PreAuthorize("hasPermission(#variationId, 'ProductVariation', 'delete')")
    public ResponseEntity<?> deleteVariation(@PathVariable Long variationId) {
        if(service.deleteVariation(variationId)) {
            return new ResponseEntity<>("Variation " + variationId + " successfully deleted.", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
