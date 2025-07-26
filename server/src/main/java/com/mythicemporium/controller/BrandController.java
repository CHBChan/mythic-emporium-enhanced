package com.mythicemporium.controller;

import com.mythicemporium.dto.BrandRequestDTO;
import com.mythicemporium.model.Brand;
import com.mythicemporium.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private BrandService service;

    public BrandController(BrandService service) {
        this.service = service;
    }

    @Operation(summary = "Fetches all brands")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all brands")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Brand>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Fetches brand by id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved brand by id")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @GetMapping("/{brandId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> findById(@PathVariable Long brandId) {
        Brand brand = service.findById(brandId);

        if(brand == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(brand);
    }

    @Operation(summary = "Adds a brand and returns it")
    @ApiResponse(responseCode = "200", description = "Successfully added brand")
    @ApiResponse(responseCode = "400", description = "Failed to add brand")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PostMapping
    @PreAuthorize("hasPermission(#brandRequestDTO, 'create')")
    public CompletableFuture<ResponseEntity<?>> createBrand(@RequestBody BrandRequestDTO brandRequestDTO) {
        return service.createBrand(brandRequestDTO)
                .thenApply(result -> {
                   if(!result.isSuccess()) {
                       return new ResponseEntity<>(result.getErrorMessages(), HttpStatus.BAD_REQUEST);
                   }
                   return new ResponseEntity<>(result.getData(), HttpStatus.CREATED);
                });
    }

    @Operation(summary = "Updates a brand")
    @ApiResponse(responseCode = "200", description = "Successfully updated brand")
    @ApiResponse(responseCode = "400", description = "Failed to update brand")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PutMapping("/{brandId}")
    @PreAuthorize("hasPermission(#brandRequestDTO, 'update')")
    public CompletableFuture<ResponseEntity<?>> updateBrand(@PathVariable Long brandId, @RequestBody BrandRequestDTO brandRequestDTO) {
        return service.updateBrand(brandId, brandRequestDTO)
                .thenApply(result -> {
                    if(!result.isSuccess()) {
                        return new ResponseEntity<>(result.getErrorMessages(), HttpStatus.BAD_REQUEST);
                    }
                    return ResponseEntity.ok(result.getData());
                });
    }

    @Operation(summary = "Deletes a brand")
    @ApiResponse(responseCode = "204", description = "Successfully deleted brand")
    @ApiResponse(responseCode = "400", description = "Failed to delete brand")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @DeleteMapping("/{brandId}")
    @PreAuthorize("hasPermission(#brandId, 'Brand', 'delete')")
    public ResponseEntity<?> deleteBrand(@PathVariable Long brandId) {
        if(service.deleteBrand(brandId)) {
            return new ResponseEntity<>("Brand " + brandId + " successfully deleted.", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
