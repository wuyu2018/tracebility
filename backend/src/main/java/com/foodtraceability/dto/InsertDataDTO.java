package com.foodtraceability.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InsertDataDTO {

    public static class MaterialPurchaseDTO {
        @NotNull(message = "产品ID不能为空")
        private Long productId;
        @NotBlank(message = "原料名称不能为空")
        @Size(max = 100, message = "原料名称长度不能超过100")
        private String materialName;
        @Size(max = 100, message = "生产商名称长度不能超过100")
        private String producerName;
        @Size(max = 255, message = "生产商地址长度不能超过255")
        private String producerAddress;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getProducerName() { return producerName; }
        public void setProducerName(String producerName) { this.producerName = producerName; }
        public String getProducerAddress() { return producerAddress; }
        public void setProducerAddress(String producerAddress) { this.producerAddress = producerAddress; }
    }

    public static class InspectionDTO {
        @NotNull(message = "产品ID不能为空")
        private Long productId;
        @Size(max = 100, message = "样品名称长度不能超过100")
        private String sampleName;
        @Min(value = 0, message = "样品数量不能小于0")
        private Integer sampleQuantity;
        @Size(max = 100, message = "样品规格长度不能超过100")
        private String sampleSpecification;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getSampleName() { return sampleName; }
        public void setSampleName(String sampleName) { this.sampleName = sampleName; }
        public Integer getSampleQuantity() { return sampleQuantity; }
        public void setSampleQuantity(Integer sampleQuantity) { this.sampleQuantity = sampleQuantity; }
        public String getSampleSpecification() { return sampleSpecification; }
        public void setSampleSpecification(String sampleSpecification) { this.sampleSpecification = sampleSpecification; }
    }

    public static class ProductDTO {
        @NotBlank(message = "防伪码不能为空")
        @Size(max = 20, message = "防伪码长度不能超过20")
        private String antiFakeCode;
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 100, message = "产品名称长度不能超过100")
        private String name;
        @Size(max = 50, message = "规格长度不能超过50")
        private String specification;
        @Size(max = 50, message = "批号长度不能超过50")
        private String batchNumber;
        private LocalDate productionDate;
        @Size(max = 50, message = "保质期长度不能超过50")
        private String shelfLife;
        @Size(max = 255, message = "图片URL长度不能超过255")
        private String imageUrl;
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        @Size(max = 20, message = "联系电话长度不能超过20")
        private String contactPhone;
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100")
        private String contactEmail;

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
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    }

    public static class StorageDTO {
        @NotNull(message = "产品ID不能为空")
        private Long productId;
        private LocalDateTime storageTime;
        private LocalDateTime outboundTime;
        @Min(value = 0, message = "数量不能小于0")
        private Double quantity;
        @Size(max = 20, message = "单位长度不能超过20")
        private String unit;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public LocalDateTime getStorageTime() { return storageTime; }
        public void setStorageTime(LocalDateTime storageTime) { this.storageTime = storageTime; }
        public LocalDateTime getOutboundTime() { return outboundTime; }
        public void setOutboundTime(LocalDateTime outboundTime) { this.outboundTime = outboundTime; }
        public Double getQuantity() { return quantity; }
        public void setQuantity(Double quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public static class TransportSaleDTO {
        @NotNull(message = "产品ID不能为空")
        private Long productId;
        private Double environmentTemperature;
        private Double productTemperature;
        private LocalDateTime time;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Double getEnvironmentTemperature() { return environmentTemperature; }
        public void setEnvironmentTemperature(Double environmentTemperature) { this.environmentTemperature = environmentTemperature; }
        public Double getProductTemperature() { return productTemperature; }
        public void setProductTemperature(Double productTemperature) { this.productTemperature = productTemperature; }
        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }
    }
}