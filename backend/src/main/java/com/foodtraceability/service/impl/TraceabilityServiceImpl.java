package com.foodtraceability.service.impl;

import com.foodtraceability.dto.TraceInfoDTO;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.TraceabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TraceabilityServiceImpl implements TraceabilityService {

    @Autowired
    private SecurityCodeRepository securityCodeRepository;

    @Autowired
    private ProductionBatchRepository batchRepository;

    @Autowired
    private BatchMaterialRelationRepository relationRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private TransportSaleRepository transportSaleRepository;

    @Override
    @Transactional
    public Optional<TraceInfoDTO> getTraceInfoByCode(String code) {
        Optional<SecurityCode> securityCodeOpt = securityCodeRepository.findByCode(code);
        
        if (securityCodeOpt.isEmpty()) {
            return Optional.empty();
        }

        SecurityCode securityCode = securityCodeOpt.get();
        ProductionBatch batch = securityCode.getBatch();
        Product product = batch.getProduct();

        if ("未激活".equals(securityCode.getStatus())) {
            securityCode.setStatus("已激活");
            securityCode.setFirstScanTime(LocalDateTime.now());
            securityCode.setScanCount(1);
            securityCodeRepository.save(securityCode);
        } else {
            securityCode.setScanCount(securityCode.getScanCount() + 1);
            securityCodeRepository.save(securityCode);
        }

        return Optional.of(buildTraceInfoDTO(product, batch, securityCode));
    }

    @Override
    @Transactional
    public Optional<TraceInfoDTO> getTraceInfoByBatchNumber(String batchNumber) {
        Optional<ProductionBatch> batchOpt = batchRepository.findByBatchNumberAndIsDeletedFalse(batchNumber);
        
        if (batchOpt.isEmpty()) {
            return Optional.empty();
        }

        ProductionBatch batch = batchOpt.get();
        Product product = batch.getProduct();

        return Optional.of(buildTraceInfoDTO(product, batch, null));
    }

    private TraceInfoDTO buildTraceInfoDTO(Product product, ProductionBatch batch, SecurityCode securityCode) {
        TraceInfoDTO dto = new TraceInfoDTO();

        TraceInfoDTO.ProductInfo productInfo = new TraceInfoDTO.ProductInfo();
        productInfo.setId(product.getId());
        productInfo.setName(product.getName());
        productInfo.setSpecification(product.getSpecification());
        productInfo.setShelfLife(product.getShelfLife());
        productInfo.setImageUrl(product.getImageUrl());
        productInfo.setContactPhone(product.getContactPhone());
        productInfo.setContactEmail(product.getContactEmail());
        dto.setProduct(productInfo);

        TraceInfoDTO.BatchInfo batchInfo = new TraceInfoDTO.BatchInfo();
        batchInfo.setId(batch.getId());
        batchInfo.setBatchNumber(batch.getBatchNumber());
        batchInfo.setProductionDate(batch.getProductionDate());
        batchInfo.setShelfLife(batch.getShelfLife());
        batchInfo.setCreatedAt(batch.getCreatedAt());
        dto.setBatch(batchInfo);

        List<BatchMaterialRelation> relations = relationRepository.findByBatchId(batch.getId());
        List<TraceInfoDTO.MaterialInfo> materials = relations.stream()
                .map(r -> {
                    MaterialPurchase m = r.getMaterial();
                    TraceInfoDTO.MaterialInfo mi = new TraceInfoDTO.MaterialInfo();
                    mi.setMaterialName(m.getMaterialName());
                    mi.setBatchNumber(m.getBatchNumber());
                    mi.setSupplierName(m.getSupplierName());
                    mi.setProducerName(m.getProducerName());
                    return mi;
                })
                .collect(Collectors.toList());
        dto.setMaterials(materials);

        List<Inspection> inspections = inspectionRepository.findAll().stream()
                .filter(i -> i.getBatch().getId().equals(batch.getId()))
                .toList();
        if (!inspections.isEmpty()) {
            Inspection inspection = inspections.get(0);
            TraceInfoDTO.InspectionInfo inspectionInfo = new TraceInfoDTO.InspectionInfo();
            inspectionInfo.setSampleName(inspection.getSampleName());
            inspectionInfo.setSampleQuantity(inspection.getSampleQuantity());
            inspectionInfo.setSampleSpecification(inspection.getSampleSpecification());
            inspectionInfo.setImageUrl(inspection.getImageUrl());
            dto.setInspection(inspectionInfo);
        }

        if (batch.getStorageId() != null) {
            storageRepository.findById(batch.getStorageId()).ifPresent(storage -> {
                TraceInfoDTO.StorageInfo storageInfo = new TraceInfoDTO.StorageInfo();
                storageInfo.setStorageTime(storage.getStorageTime());
                storageInfo.setOutboundTime(storage.getOutboundTime());
                storageInfo.setWarehouseLocation(storage.getWarehouseLocation());
                dto.setStorage(storageInfo);
            });
        }

        if (batch.getTransportSaleId() != null) {
            transportSaleRepository.findById(batch.getTransportSaleId()).ifPresent(ts -> {
                TraceInfoDTO.TransportSaleInfo transportInfo = new TraceInfoDTO.TransportSaleInfo();
                transportInfo.setTransportTime(ts.getTime());
                transportInfo.setTransportCompany(null);
                transportInfo.setVehicleNumber(null);
                transportInfo.setSalesRegion(ts.getSalesRegion());
                transportInfo.setReceiverName(null);
                dto.setTransportSale(transportInfo);
            });
        }

        if (securityCode != null) {
            dto.setStatus(securityCode.getStatus());
            dto.setFirstScanTime(securityCode.getFirstScanTime());
            dto.setScanCount(securityCode.getScanCount());
            
            // 判断是否被查询过
            if (securityCode.getScanCount() != null && securityCode.getScanCount() > 1) {
                dto.setIsQueried(true);
                dto.setQueryTip("该产品已被查询过 " + (securityCode.getScanCount() - 1) + " 次，首次查询时间：" + securityCode.getFirstScanTime());
            } else {
                dto.setIsQueried(false);
                dto.setQueryTip(null);
            }
        } else {
            dto.setStatus("未扫码");
            dto.setIsQueried(false);
            dto.setQueryTip(null);
        }

        return dto;
    }
}