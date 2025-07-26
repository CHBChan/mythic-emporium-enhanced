package com.mythicemporium.repository;

import com.mythicemporium.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findByName(String name);

    @Modifying
    @Query("DELETE FROM Brand b WHERE b.id = :id")
    int deleteByIdAndReturnCount(@Param("id") Long id);
}
