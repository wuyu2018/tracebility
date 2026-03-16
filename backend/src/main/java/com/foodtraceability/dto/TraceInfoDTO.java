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
    private List<MaterialPurchaseInfo> materialPurchases;
    private List<StorageInfo> storages;
    private List<InspectionInfo> inspections;
    private List<TransportSaleInfo> transportSales;
    private List<ComplaintInfo> complaints;

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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialPurchaseInfo {
        private String materialName;
        private String producerName;
        private String producerAddress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StorageInfo {
        private LocalDateTime storageTime;
        private LocalDateTime outboundTime;
        private Double quantity;
        private String unit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InspectionInfo {
        private String sampleName;
        private Integer sampleQuantity;
        private String sampleSpecification;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransportSaleInfo {
        private Double environmentTemperature;
        private Double productTemperature;
        private LocalDateTime time;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplaintInfo {
        private String complaintReason;
        private LocalDateTime complaintTime;
    }
}
