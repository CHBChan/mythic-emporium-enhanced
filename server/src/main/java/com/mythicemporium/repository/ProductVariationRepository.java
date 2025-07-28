package com.mythicemporium.repository;


import com.mythicemporium.model.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, Long> {
    @Modifying
    @Query("UPDATE ProductVariation pv SET pv.stock = pv.stock - :quantity WHERE pv.id = :id AND pv.stock >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE ProductVariation p SET p.stock = :stock WHERE p.id = :id")
    int updateStockById(@Param("id") Long id, @Param("stock") Integer stock);

    @Modifying
    @Query("UPDATE ProductVariation p SET p.price = :price WHERE p.id = :id")
    int updatePriceById(@Param("id") Long id, @Param("price") Double price);
}
