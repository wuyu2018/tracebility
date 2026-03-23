package com.foodtraceability.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InsertDataDTO {

    @Data
    public static class MaterialPurchaseDTO {
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 100, message = "产品名称长度不能超过100")
        private String productName;
        @Size(max = 50, message = "批号长度不能超过50")
        private String batchNumber;
        @NotBlank(message = "原料名称不能为空")
        @Size(max = 100, message = "原料名称长度不能超过100")
        private String materialName;
        @Size(max = 100, message = "生产商名称长度不能超过100")
        private String producerName;
        @Size(max = 255, message = "生产商地址长度不能超过255")
        private String producerAddress;
    }

    @Data
    public static class InspectionDTO {
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 100, message = "产品名称长度不能超过100")
        private String productName;
        @Size(max = 50, message = "批号长度不能超过50")
        private String batchNumber;
        @Size(max = 100, message = "样品名称长度不能超过100")
        private String sampleName;
        @Min(value = 0, message = "样品数量不能小于0")
        private Integer sampleQuantity;
        @Size(max = 100, message = "样品规格长度不能超过100")
        private String sampleSpecification;
    }

    @Data
    public static class ProductDTO {
        @NotBlank(message = "防伪码不能为空")
        @Size(max = 20, message = "防伪码长度不能超过20")
        private String antiFakeCode;
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 100, message = "产品名称长度不能超过100")
        private String name;

        @Size(max = 50, message = "规格长度不能超过50")
        private String specification;
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
    }

    @Data
    public static class StorageDTO {
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 100, message = "产品名称长度不能超过100")
        private String productName;
        @Size(max = 50, message = "批号长度不能超过50")
        private String batchNumber;
        private LocalDateTime storageTime;
        private LocalDateTime outboundTime;
        @Min(value = 0, message = "数量不能小于0")
        private Double quantity;
        @Size(max = 20, message = "单位长度不能超过20")
        private String unit;
    }

    @Data
    public static class TransportSaleDTO {
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 100, message = "产品名称长度不能超过100")
        private String productName;
        @Size(max = 50, message = "批号长度不能超过50")
        private String batchNumber;
        private Double environmentTemperature;
        private Double productTemperature;
        private LocalDateTime time;
    }
}