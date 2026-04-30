package com.foodtraceability.domain.valueobject;

import com.foodtraceability.entity.MaterialPurchase;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MaterialInfo {

    private final String materialName;
    private final String batchNumber;
    private final String supplierName;
    private final String producerName;

    public static MaterialInfo from(MaterialPurchase material) {
        if (material == null) {
            return null;
        }
        return new MaterialInfo(
            material.getMaterial() != null ? material.getMaterial().getName() : null,
            material.getBatchNumber(),
            material.getSupplierName(),
            material.getProducerName()
        );
    }
}
