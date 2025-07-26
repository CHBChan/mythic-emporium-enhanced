package com.mythicemporium.controller;

import com.mythicemporium.dto.CategoryRequestDTO;
import com.mythicemporium.model.Category;
import com.mythicemporium.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @Operation(summary = "Fetches all categories")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all categorys")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Fetches category by id")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved category by id")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @GetMapping("/{categoryId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> findById(@PathVariable Long categoryId) {
        Category category = service.findById(categoryId);

        if(category == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "Adds a category and returns it")
    @ApiResponse(responseCode = "200", description = "Successfully added category")
    @ApiResponse(responseCode = "400", description = "Failed to add category")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PostMapping
    @PreAuthorize("hasPermission(#categoryRequestDTO, 'create')")
    public CompletableFuture<ResponseEntity<?>> createCategory(@RequestBody CategoryRequestDTO categoryRequestDTO) {
        return service.createCategory(categoryRequestDTO)
                .thenApply(result -> {
                   if(!result.isSuccess()) {
                       return new ResponseEntity<>(result.getErrorMessages(), HttpStatus.BAD_REQUEST);
                   }
                   return new ResponseEntity<>(result.getData(), HttpStatus.CREATED);
                });
    }

    @Operation(summary = "Updates a category")
    @ApiResponse(responseCode = "200", description = "Successfully updated category")
    @ApiResponse(responseCode = "400", description = "Failed to update category")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasPermission(#categoryRequestDTO, 'update')")
    public CompletableFuture<ResponseEntity<?>> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryRequestDTO categoryRequestDTO) {
        return service.updateCategory(categoryId, categoryRequestDTO)
                .thenApply(result -> {
                    if(!result.isSuccess()) {
                        return new ResponseEntity<>(result.getErrorMessages(), HttpStatus.BAD_REQUEST);
                    }
                    return ResponseEntity.ok(result.getData());
                });
    }

    @Operation(summary = "Deletes a category")
    @ApiResponse(responseCode = "204", description = "Successfully deleted category")
    @ApiResponse(responseCode = "400", description = "Failed to delete category")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "500", description = "Failed to connect to database")
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasPermission(#categoryId, 'Category', 'delete')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        if(service.deleteCategory(categoryId)) {
            return new ResponseEntity<>("Category " + categoryId + " successfully deleted.", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
