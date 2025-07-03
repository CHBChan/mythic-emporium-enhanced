package com.mythicemporium.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductVariationResponseDTO {
    private Long id;
    private String sku;
    private Double price;
    private Integer stock;
    private String imageUrl;

    private List<ProductVariationAttributeDTO> attributes;
}
