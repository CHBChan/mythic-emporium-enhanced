package com.mythicemporium.repository;

import com.mythicemporium.model.Brand;
import com.mythicemporium.model.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId")
    List<Product> findAllByBrandId(@Param("brandId") Long brandId);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findAllByCategoryId(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("DELETE FROM Product p WHERE p.id = :id")
    int deleteByIdAndReturnCount(@Param("id") Long id);
}
