package com.foodtraceability.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TraceInfoDTO {

    private ProductInfo product;
    private List<MaterialPurchaseDTO> materialPurchases;
    private List<StorageDTO> storages;
    private List<InspectionDTO> inspections;
    private List<TransportSaleDTO> transportSales;
    private List<ComplaintDTO> complaints;

    // Product info nested
    public static class ProductInfo {
        private Long id;
        private String antiFakeCode;
        private String name;
        private String specification;
        private String batchNumber;
        private LocalDate productionDate;
        private String shelfLife;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getAntiFakeCode() { return antiFakeCode; }
        public void setAntiFakeCode(String antiFakeCode) { this.antiFakeCode = antiFakeCode; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        public LocalDate getProductionDate() { return productionDate; }
        public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }
        public String getShelfLife() { return shelfLife; }
        public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }
    }

    public static class MaterialPurchaseDTO {
        private String materialName;
        private String producerName;
        private String producerAddress;

        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getProducerName() { return producerName; }
        public void setProducerName(String producerName) { this.producerName = producerName; }
        public String getProducerAddress() { return producerAddress; }
        public void setProducerAddress(String producerAddress) { this.producerAddress = producerAddress; }
    }

    public static class StorageDTO {
        private LocalDateTime storageTime;
        private LocalDateTime outboundTime;
        private Double quantity;
        private String unit;

        public LocalDateTime getStorageTime() { return storageTime; }
        public void setStorageTime(LocalDateTime storageTime) { this.storageTime = storageTime; }
        public LocalDateTime getOutboundTime() { return outboundTime; }
        public void setOutboundTime(LocalDateTime outboundTime) { this.outboundTime = outboundTime; }
        public Double getQuantity() { return quantity; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public static class InspectionDTO {
        private String sampleName;
        private Integer sampleQuantity;
        private String sampleSpecification;

        public String getSampleName() { return sampleName; }
        public void setSampleName(String sampleName) { this.sampleName = sampleName; }
        public Integer getSampleQuantity() { return sampleQuantity; }
        public void setSampleQuantity(Integer sampleQuantity) { this.sampleQuantity = sampleQuantity; }
        public String getSampleSpecification() { return sampleSpecification; }
        public void setSampleSpecification(String sampleSpecification) { this.sampleSpecification = sampleSpecification; }
    }

    public static class TransportSaleDTO {
        private Double environmentTemperature;
        private Double productTemperature;
        private LocalDateTime time;

        public Double getEnvironmentTemperature() { return environmentTemperature; }
        public void setEnvironmentTemperature(Double environmentTemperature) { this.environmentTemperature = environmentTemperature; }
        public Double getProductTemperature() { return productTemperature; }
        public void setProductTemperature(Double productTemperature) { this.productTemperature = productTemperature; }
        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }
    }

    public static class ComplaintDTO {
        private String complaintReason;
        private LocalDateTime complaintTime;

        public String getComplaintReason() { return complaintReason; }
        public void setComplaintReason(String complaintReason) { this.complaintReason = complaintReason; }
        public LocalDateTime getComplaintTime() { return complaintTime; }
        public void setComplaintTime(LocalDateTime complaintTime) { this.complaintTime = complaintTime; }
    }

    // Getters and Setters for TraceInfoDTO
    public ProductInfo getProduct() { return product; }
    public void setProduct(ProductInfo product) { this.product = product; }
    public List<MaterialPurchaseDTO> getMaterialPurchases() { return materialPurchases; }
    public void setMaterialPurchases(List<MaterialPurchaseDTO> materialPurchases) { this.materialPurchases = materialPurchases; }
    public List<StorageDTO> getStorages() { return storages; }
    public void setStorages(List<StorageDTO> storages) { this.storages = storages; }
    public List<InspectionDTO> getInspections() { return inspections; }
    public void setInspections(List<InspectionDTO> inspections) { this.inspections = inspections; }
    public List<TransportSaleDTO> getTransportSales() { return transportSales; }
    public void setTransportSales(List<TransportSaleDTO> transportSales) { this.transportSales = transportSales; }
    public List<ComplaintDTO> getComplaints() { return complaints; }
    public void setComplaints(List<ComplaintDTO> complaints) { this.complaints = complaints; }
}
