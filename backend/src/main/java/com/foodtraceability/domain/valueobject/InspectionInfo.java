package com.foodtraceability.domain.valueobject;

import com.foodtraceability.entity.Inspection;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InspectionInfo {

    private final String sampleName;
    private final Integer sampleQuantity;
    private final String sampleSpecification;
    private final String imageUrl;

    public static InspectionInfo from(Inspection inspection) {
        if (inspection == null) {
            return null;
        }
        return new InspectionInfo(
            inspection.getSampleName(),
            inspection.getSampleQuantity(),
            inspection.getSampleSpecification(),
            inspection.getImageUrl()
        );
    }
}
