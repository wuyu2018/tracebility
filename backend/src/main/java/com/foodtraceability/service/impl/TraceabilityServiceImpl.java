package com.foodtraceability.service.impl;

import com.foodtraceability.dto.TraceInfoDTO;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.TraceabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TraceabilityServiceImpl implements TraceabilityService {

    private final SecurityCodeRepository securityCodeRepository;
    private final ProductionBatchRepository batchRepository;
    private final BatchMaterialRelationRepository relationRepository;
    private final InspectionRepository inspectionRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;

    @Autowired
    public TraceabilityServiceImpl(SecurityCodeRepository securityCodeRepository,
                                   ProductionBatchRepository batchRepository,
                                   BatchMaterialRelationRepository relationRepository,
                                   InspectionRepository inspectionRepository,
                                   StorageRepository storageRepository,
                                   TransportSaleRepository transportSaleRepository) {
        this.securityCodeRepository = securityCodeRepository;
        this.batchRepository = batchRepository;
        this.relationRepository = relationRepository;
        this.inspectionRepository = inspectionRepository;
        this.storageRepository = storageRepository;
        this.transportSaleRepository = transportSaleRepository;
    }

    @Override
    @Transactional
    public Optional<TraceInfoDTO> getTraceInfoByCode(String code) {
        Optional<SecurityCode> securityCodeOpt = securityCodeRepository.findByCode(code);

        if (securityCodeOpt.isEmpty()) {
            return Optional.empty();
        }

        SecurityCode securityCode = securityCodeOpt.get();
        securityCode.recordQueryAndActivateIfNeeded();
        securityCodeRepository.save(securityCode);

        ProductionBatch batch = securityCode.getBatch();
        Product product = batch.getProduct();

        return Optional.of(buildTraceInfoDTO(product, batch, securityCode, false));
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

        return Optional.of(buildTraceInfoDTO(product, batch, null, false));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TraceInfoDTO> getTraceInfoByCodeForAdmin(String code) {
        Optional<SecurityCode> securityCodeOpt = securityCodeRepository.findByCode(code);

        if (securityCodeOpt.isEmpty()) {
            return Optional.empty();
        }

        SecurityCode securityCode = securityCodeOpt.get();
        ProductionBatch batch = securityCode.getBatch();
        Product product = batch.getProduct();

        return Optional.of(buildTraceInfoDTO(product, batch, securityCode, true));
    }

    private TraceInfoDTO buildTraceInfoDTO(Product product, ProductionBatch batch, SecurityCode securityCode, boolean forAdmin) {
        TraceInfoDTO dto = new TraceInfoDTO();

        dto.setProduct(buildProductInfo(product));
        dto.setBatch(buildBatchInfo(batch));
        dto.setMaterials(buildMaterialInfos(batch.getId()));
        dto.setInspection(findInspection(batch.getId()));
        dto.setStorage(findStorage(batch.getStorageId()));
        dto.setTransportSale(findTransportSale(batch.getTransportSaleId(), forAdmin));

        if (securityCode != null) {
            dto.setStatus(securityCode.getStatus());
            dto.setFirstScanTime(securityCode.getFirstScanTime());
            dto.setScanCount(securityCode.getQueryCount());
            dto.setIsQueried(securityCode.isRepeatedQuery());
            dto.setQueryTip(buildQueryTip(securityCode));
        } else {
            dto.setStatus("未扫码");
            dto.setIsQueried(false);
            dto.setQueryTip(null);
        }

        return dto;
    }

    private TraceInfoDTO.ProductInfo buildProductInfo(Product product) {
        TraceInfoDTO.ProductInfo productInfo = new TraceInfoDTO.ProductInfo();
        productInfo.setId(product.getId());
        productInfo.setName(product.getName());
        productInfo.setSpecification(product.getSpecification());
        productInfo.setShelfLife(product.getShelfLife());
        productInfo.setImageUrl(product.getImageUrl());
        productInfo.setContactPhone(product.getContactPhone());
        productInfo.setContactEmail(product.getContactEmail());
        productInfo.setAntiFakeCode(product.getAntiFakeCode());
        return productInfo;
    }

    private TraceInfoDTO.BatchInfo buildBatchInfo(ProductionBatch batch) {
        TraceInfoDTO.BatchInfo batchInfo = new TraceInfoDTO.BatchInfo();
        batchInfo.setId(batch.getId());
        batchInfo.setBatchNumber(batch.getBatchNumber());
        batchInfo.setProductionDate(batch.getProductionDate());
        batchInfo.setShelfLife(batch.getShelfLife());
        batchInfo.setCreatedAt(batch.getCreatedAt());
        return batchInfo;
    }

    private List<TraceInfoDTO.MaterialInfo> buildMaterialInfos(Long batchId) {
        List<BatchMaterialRelation> relations = relationRepository.findByBatchId(batchId);
        return relations.stream()
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
    }

    private TraceInfoDTO.InspectionInfo findInspection(Long batchId) {
        List<Inspection> inspections = inspectionRepository.findAll().stream()
                .filter(i -> batchId.equals(i.getBatch().getId()))
                .toList();
        if (inspections.isEmpty()) {
            return null;
        }
        Inspection inspection = inspections.get(0);
        TraceInfoDTO.InspectionInfo inspectionInfo = new TraceInfoDTO.InspectionInfo();
        inspectionInfo.setSampleName(inspection.getSampleName());
        inspectionInfo.setSampleQuantity(inspection.getSampleQuantity());
        inspectionInfo.setSampleSpecification(inspection.getSampleSpecification());
        inspectionInfo.setImageUrl(inspection.getImageUrl());
        return inspectionInfo;
    }

    private TraceInfoDTO.StorageInfo findStorage(Long storageId) {
        if (storageId == null) {
            return null;
        }
        return storageRepository.findById(storageId)
                .map(storage -> {
                    TraceInfoDTO.StorageInfo storageInfo = new TraceInfoDTO.StorageInfo();
                    storageInfo.setStorageTime(storage.getStorageTime());
                    storageInfo.setOutboundTime(storage.getOutboundTime());
                    storageInfo.setWarehouseLocation(storage.getWarehouseLocation());
                    return storageInfo;
                })
                .orElse(null);
    }

    private TraceInfoDTO.TransportSaleInfo findTransportSale(Long transportSaleId, boolean forAdmin) {
        if (transportSaleId == null) {
            return null;
        }
        return transportSaleRepository.findById(transportSaleId)
                .map(ts -> {
                    TraceInfoDTO.TransportSaleInfo transportInfo = new TraceInfoDTO.TransportSaleInfo();
                    transportInfo.setTransportTime(ts.getTime());
                    if (forAdmin) {
                        transportInfo.setTransportCompany(ts.getTransportCompany());
                        transportInfo.setVehicleNumber(ts.getVehicleNumber());
                        transportInfo.setReceiverName(ts.getReceiverName());
                        transportInfo.setReceiverContact(ts.getReceiverContact());
                    }
                    transportInfo.setSalesRegion(ts.getSalesRegion());
                    return transportInfo;
                })
                .orElse(null);
    }

    private String buildQueryTip(SecurityCode securityCode) {
        if (!securityCode.isRepeatedQuery()) {
            return null;
        }
        return String.format("该产品已被查询过 %d 次，首次查询时间：%s。重复查询可能是伪品，请谨慎购买！",
                securityCode.getQueryCount() - 1,
                securityCode.getFirstScanTime());
    }
}