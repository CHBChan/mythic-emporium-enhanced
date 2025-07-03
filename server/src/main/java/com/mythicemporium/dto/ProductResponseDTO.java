package com.mythicemporium.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;

    private String brandName;
    private String categoryName;

    private List<ProductVariationResponseDTO> variations;
}
