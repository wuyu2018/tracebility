package com.foodtraceability.dto;

import com.foodtraceability.domain.valueobject.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TraceInfoConverter {

    public static TraceInfoDTO toDTO(
            TraceInfo traceInfo,
            String status,
            LocalDateTime firstScanTime,
            int scanCount,
            boolean forAdmin) {

        TraceInfoDTO dto = new TraceInfoDTO();
        dto.setProduct(toProductInfo(traceInfo.getProduct()));
        dto.setBatch(toBatchInfo(traceInfo.getBatch()));
        dto.setMaterials(toMaterialInfos(traceInfo.getMaterials()));
        dto.setInspection(toInspectionInfo(traceInfo.getInspection()));
        dto.setStorage(toStorageInfo(traceInfo.getStorage()));
        dto.setTransportSale(toTransportSaleInfo(traceInfo.getTransportSale()));

        dto.setStatus(status);
        dto.setFirstScanTime(firstScanTime);
        dto.setScanCount(scanCount);
        dto.setIsQueried(scanCount > 1);
        dto.setQueryTip(buildQueryTip(scanCount, firstScanTime));

        return dto;
    }

    private static TraceInfoDTO.ProductInfo toProductInfo(ProductInfo productInfo) {
        if (productInfo == null) {
            return null;
        }
        TraceInfoDTO.ProductInfo dto = new TraceInfoDTO.ProductInfo();
        dto.setId(productInfo.getId());
        dto.setName(productInfo.getName());
        dto.setSpecification(productInfo.getSpecification());
        dto.setShelfLife(productInfo.getShelfLife());
        dto.setImageUrl(productInfo.getImageUrl());
        dto.setContactPhone(productInfo.getContactPhone());
        dto.setContactEmail(productInfo.getContactEmail());
        dto.setAntiFakeCode(productInfo.getAntiFakeCode());
        return dto;
    }

    private static TraceInfoDTO.BatchInfo toBatchInfo(BatchInfo batchInfo) {
        if (batchInfo == null) {
            return null;
        }
        TraceInfoDTO.BatchInfo dto = new TraceInfoDTO.BatchInfo();
        dto.setId(batchInfo.getId());
        dto.setBatchNumber(batchInfo.getBatchNumber());
        dto.setProductionDate(batchInfo.getProductionDate());
        dto.setShelfLife(batchInfo.getShelfLife());
        dto.setCreatedAt(batchInfo.getCreatedAt());
        return dto;
    }

    private static List<TraceInfoDTO.MaterialInfo> toMaterialInfos(List<MaterialInfo> materialInfos) {
        if (materialInfos == null) {
            return List.of();
        }
        return materialInfos.stream()
                .map(TraceInfoConverter::toMaterialInfo)
                .collect(Collectors.toList());
    }

    private static TraceInfoDTO.MaterialInfo toMaterialInfo(MaterialInfo materialInfo) {
        if (materialInfo == null) {
            return null;
        }
        TraceInfoDTO.MaterialInfo dto = new TraceInfoDTO.MaterialInfo();
        dto.setMaterialName(materialInfo.getMaterialName());
        dto.setBatchNumber(materialInfo.getBatchNumber());
        dto.setSupplierName(materialInfo.getSupplierName());
        dto.setProducerName(materialInfo.getProducerName());
        return dto;
    }

    private static TraceInfoDTO.InspectionInfo toInspectionInfo(InspectionInfo inspectionInfo) {
        if (inspectionInfo == null) {
            return null;
        }
        TraceInfoDTO.InspectionInfo dto = new TraceInfoDTO.InspectionInfo();
        dto.setSampleName(inspectionInfo.getSampleName());
        dto.setSampleQuantity(inspectionInfo.getSampleQuantity());
        dto.setSampleSpecification(inspectionInfo.getSampleSpecification());
        dto.setImageUrl(inspectionInfo.getImageUrl());
        return dto;
    }

    private static TraceInfoDTO.StorageInfo toStorageInfo(StorageInfo storageInfo) {
        if (storageInfo == null) {
            return null;
        }
        TraceInfoDTO.StorageInfo dto = new TraceInfoDTO.StorageInfo();
        dto.setStorageTime(storageInfo.getStorageTime());
        dto.setOutboundTime(storageInfo.getOutboundTime());
        dto.setWarehouseLocation(storageInfo.getWarehouseLocation());
        return dto;
    }

    private static TraceInfoDTO.TransportSaleInfo toTransportSaleInfo(TransportSaleInfo transportSaleInfo) {
        if (transportSaleInfo == null) {
            return null;
        }
        TraceInfoDTO.TransportSaleInfo dto = new TraceInfoDTO.TransportSaleInfo();
        dto.setTransportTime(transportSaleInfo.getTransportTime());
        dto.setTransportCompany(transportSaleInfo.getTransportCompany());
        dto.setVehicleNumber(transportSaleInfo.getVehicleNumber());
        dto.setReceiverName(transportSaleInfo.getReceiverName());
        dto.setReceiverContact(transportSaleInfo.getReceiverContact());
        dto.setSalesRegion(transportSaleInfo.getSalesRegion());
        return dto;
    }

    private static String buildQueryTip(int scanCount, LocalDateTime firstScanTime) {
        if (scanCount <= 1) {
            return null;
        }
        return String.format("该产品已被查询过 %d 次，首次查询时间：%s。重复查询可能是伪品，请谨慎购买！",
                scanCount - 1,
                firstScanTime);
    }
}
