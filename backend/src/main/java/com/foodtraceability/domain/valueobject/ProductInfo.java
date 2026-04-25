package com.foodtraceability.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductInfo {

    private final Long id;
    private final String name;
    private final String specification;
    private final String shelfLife;
    private final String imageUrl;
    private final String contactPhone;
    private final String contactEmail;
    private final String antiFakeCode;

    public static ProductInfo from(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductInfo(
            product.getId(),
            product.getName(),
            product.getSpecification(),
            product.getShelfLife(),
            product.getImageUrl(),
            product.getContactPhone(),
            product.getContactEmail(),
            product.getAntiFakeCode()
        );
    }
}
