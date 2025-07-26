package com.mythicemporium.service;

import com.mythicemporium.dto.BrandRequestDTO;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
import com.mythicemporium.model.Brand;
import com.mythicemporium.repository.BrandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @Mock
    BrandRepository brandRepository;

    private BrandService service;

    @BeforeEach
    void setUp() {
        service = new BrandService(brandRepository);
    }

    @Test
    void shouldFindAll() {
        when(brandRepository.findAll()).thenReturn(List.of(generateBrand(1L)));
        List<Brand> brands = service.findAll();

        assertEquals(1, brands.size());
    }

    @Test
    void shouldFindValidById() {
        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.of(generateBrand(1L)));
        Brand brand = service.findById(1L);

        assertNotNull(brand);
        assertEquals("Test Brand 1", brand.getName());
    }

    @Test
    void shouldNotFindInvalidById() {
        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        Brand brand = service.findById(1L);

        assertNull(brand);
    }

    @Test
    void shouldCreateValidBrand() throws ExecutionException, InterruptedException {
        when(brandRepository.findByName(any(String.class))).thenReturn(List.of());
        when(brandRepository.save(any(Brand.class))).thenReturn(generateBrand(1L));

        BrandRequestDTO dto = generateBrandRequest();

        CompletableFuture<Result> result = service.createBrand(dto);

        assertTrue(result.isDone());
        assertNotNull(result.get());
        assertTrue(result.get().isSuccess());

        assertEquals(dto.getName(), ((Brand) result.get().getData()).getName());
    }

    @Test
    void shouldNotCreateBrandWithInvalidDTO() {
        assertThrows(InvalidRequestException.class, () -> service.createBrand(null));
    }

    @Test
    void shouldNotCreateBrandWithInvalidName() {
        BrandRequestDTO dto = generateBrandRequest();

        // Test null name
        dto.setName(null);
        assertThrows(InvalidRequestException.class, () -> service.createBrand(dto));

        // Test empty name
        dto.setName("");
        assertThrows(InvalidRequestException.class, () -> service.createBrand(dto));
    }

    @Test
    void shouldNotCreateBrandWithDuplicateName() {
        when(brandRepository.findByName(any(String.class))).thenReturn(List.of(generateBrand(1L)));

        BrandRequestDTO dto = generateBrandRequest();

        assertThrows(ResourceConflictException.class, () -> service.createBrand(dto));
    }

    @Test
    void shouldUpdateValidBrand() throws ExecutionException, InterruptedException {
        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.of(generateBrand(1L)));
        when(brandRepository.findByName(any(String.class))).thenReturn(List.of());

        BrandRequestDTO dto = generateBrandRequest();

        CompletableFuture<Result> result = service.updateBrand(1L, dto);

        assertTrue(result.isDone());
        assertNotNull(result.get());
        assertTrue(result.get().isSuccess());
        assertEquals(dto.getName(), ((Brand) result.get().getData()).getName());
    }

    @Test
    void shouldNotUpdateBrandWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.updateBrand(-1L, generateBrandRequest()));
    }

    @Test
    void shouldNotUpdateBrandWithInvalidDTO() {
        assertThrows(ResourceNotFoundException.class, () -> service.updateBrand(0L, null));
    }

    @Test
    void shouldNotUpdateBrandWithInvalidName() {
        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.of(generateBrand(1L)));

        BrandRequestDTO dto = generateBrandRequest();

        // Test null name;
        dto.setName(null);
        assertThrows(InvalidRequestException.class, () -> service.updateBrand(0L, dto));

        // Test empty name;
        dto.setName("");
        assertThrows(InvalidRequestException.class, () -> service.updateBrand(0L, dto));
    }

    @Test
    void shouldNotUpdateBrandWithDuplicateName() {
        when(brandRepository.findById(any(Long.class))).thenReturn(Optional.of(generateBrand(1L)));
        when(brandRepository.findByName(any(String.class))).thenReturn(List.of(generateBrand(1L)));

        BrandRequestDTO dto = generateBrandRequest();

        assertThrows(ResourceConflictException.class, () -> service.updateBrand(0L, dto));
    }

    @Test
    void shouldDeleteValidBrand() {
        when(brandRepository.deleteByIdAndReturnCount(any(Long.class))).thenReturn(1);

        assertTrue(service.deleteBrand(1L));
    }

    @Test
    void shouldNotDeleteBrandWithInvalidId() {
        assertThrows(InvalidRequestException.class, () -> service.deleteBrand(-1L));
    }

    @Test
    void shouldNotDeleteBrandWithNonexistentId() {
        when(brandRepository.deleteByIdAndReturnCount(any(Long.class))).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteBrand(1L));
    }

    private Brand generateBrand(Long id) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName("Test Brand " + id);
        return brand;
    }

    private BrandRequestDTO generateBrandRequest() {
        BrandRequestDTO dto = new BrandRequestDTO();
        dto.setName("Test Brand 1");
        return dto;
    }
}
