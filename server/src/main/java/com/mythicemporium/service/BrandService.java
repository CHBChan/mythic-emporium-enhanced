package com.mythicemporium.service;

import com.mythicemporium.dto.BrandRequestDTO;
import com.mythicemporium.dto.CategoryRequestDTO;
import com.mythicemporium.exception.InvalidRequestException;
import com.mythicemporium.exception.ResourceConflictException;
import com.mythicemporium.exception.ResourceNotFoundException;
import com.mythicemporium.model.Brand;
import com.mythicemporium.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class BrandService {

    private BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    public Brand findById(Long id) {
        Optional<Brand> brand = brandRepository.findById(id);

        return brand.orElse(null);
    }

    public CompletableFuture<Result> createBrand(BrandRequestDTO brandRequestDTO) {
        if(brandRequestDTO == null) {
            throw new InvalidRequestException("Brand cannot be null.");
        }

        if(brandRequestDTO.getName() == null || brandRequestDTO.getName().isBlank()) {
            throw new InvalidRequestException("Brand name cannot be null or empty.");
        }

        if(!brandRepository.findByName(brandRequestDTO.getName()).isEmpty()) {
            throw new ResourceConflictException("Brand name already exists.");
        }

        Brand brand = new Brand();
        brand.setName(brandRequestDTO.getName());

        Result result = new Result();

        try {
            brand = brandRepository.save(brand);
            result.setData(brand);
        }
        catch(Exception ex) {
            result.addErrorMessage(ex.getMessage(), ResultType.INVALID);
        }

        return CompletableFuture.completedFuture(result);
    }

    public CompletableFuture<Result> updateBrand(Long brandId, BrandRequestDTO brandRequestDTO) {
        if(brandId < 0) {
            throw new InvalidRequestException("Brand id cannot be negative.");
        }

        if(brandRepository.findById(brandId).isEmpty()) {
            throw new ResourceNotFoundException("Brand id " + brandId + " does not exist.");
        }

        if(brandRequestDTO.getName() == null || brandRequestDTO.getName().isBlank()) {
            throw new InvalidRequestException("Brand name cannot be null or empty.");
        }

        if(!brandRepository.findByName(brandRequestDTO.getName()).isEmpty()) {
            throw new ResourceConflictException("Brand name already exists.");
        }

        Brand brand = new Brand();
        brand.setId(brandId);
        brand.setName(brandRequestDTO.getName());

        Result result = new Result();
        result.setData(brand);

        return CompletableFuture.completedFuture(result);
    }

    public boolean deleteBrand(Long brandId) {
        if(brandId < 0) {
            throw new InvalidRequestException("Brand id cannot be negative.");
        }

        if(brandRepository.deleteByIdAndReturnCount(brandId) == 0) {
            throw new ResourceNotFoundException("Brand " + brandId + " not found.");
        }
        return true;
    }
}
