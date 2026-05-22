package com.foodtraceability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductMaterialRelationDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long materialId;
    private String materialName;
    private Boolean isHidden;
}
