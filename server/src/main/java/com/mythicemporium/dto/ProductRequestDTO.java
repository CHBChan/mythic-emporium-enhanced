package com.mythicemporium.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductRequestDTO {
    private String name;
    private String description;
    private Long brandId;
    private Long categoryId;

    private List<ProductVariationRequestDTO> variations;
}
