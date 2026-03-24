package com.foodtraceability.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceInfoDTO {

    private ProductInfo product;
    private List<MaterialPurchaseDTO> materialPurchases;
    private List<StorageDTO> storages;
    private List<InspectionDTO> inspections;
    private List<TransportSaleDTO> transportSales;
    private List<ComplaintDTO> complaints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String antiFakeCode;
        private String name;
        private String specification;
        private String batchNumber;
        private LocalDate productionDate;
        private String shelfLife;
        private LocalDateTime lastQueriedTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialPurchaseDTO {
        private String antiFakeCode;
        private String batchNumber;
        private String materialName;
        private String producerName;
        private String producerAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorageDTO {
        private String antiFakeCode;
        private String batchNumber;
        private LocalDateTime storageTime;
        private LocalDateTime outboundTime;
        private Double quantity;
        private String unit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectionDTO {
        private String antiFakeCode;
        private String batchNumber;
        private String sampleName;
        private Integer sampleQuantity;
        private String sampleSpecification;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransportSaleDTO {
        private String antiFakeCode;
        private String batchNumber;
        private Double environmentTemperature;
        private Double productTemperature;
        private LocalDateTime time;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplaintDTO {
        private String antiFakeCode;
        private String complaintReason;
        private LocalDateTime complaintTime;
    }
}
