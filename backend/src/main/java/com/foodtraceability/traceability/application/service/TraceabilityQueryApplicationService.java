package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.*;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TraceabilityQueryApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TraceabilityQueryApplicationService.class);

    private final SecurityCodeRepository securityCodeRepo;
    private final ProductionBatchRepository batchRepo;
    private final ProductRepository productRepo;
    private final BatchMaterialRelationRepository relationRepo;
    private final MaterialPurchaseRepository materialPurchaseRepo;
    private final MaterialRepository materialRepo;
    private final InspectionRepository inspectionRepo;
    private final StorageRepository storageRepo;
    private final TransportSaleRepository transportSaleRepo;

    public TraceabilityQueryApplicationService(SecurityCodeRepository securityCodeRepo,
                                                ProductionBatchRepository batchRepo,
                                                ProductRepository productRepo,
                                                BatchMaterialRelationRepository relationRepo,
                                                MaterialPurchaseRepository materialPurchaseRepo,
                                                MaterialRepository materialRepo,
                                                InspectionRepository inspectionRepo,
                                                StorageRepository storageRepo,
                                                TransportSaleRepository transportSaleRepo) {
        this.securityCodeRepo = securityCodeRepo;
        this.batchRepo = batchRepo;
        this.productRepo = productRepo;
        this.relationRepo = relationRepo;
        this.materialPurchaseRepo = materialPurchaseRepo;
        this.materialRepo = materialRepo;
        this.inspectionRepo = inspectionRepo;
        this.storageRepo = storageRepo;
        this.transportSaleRepo = transportSaleRepo;
    }

    @Transactional
    public TraceResult queryByCode(String code) {
        SecurityCode sc = securityCodeRepo.findByCode(code)
                .orElseThrow(() -> new BusinessException("防伪码不存在: " + code));

        sc.recordQueryAndActivateIfNeeded();
        securityCodeRepo.save(sc);

        ProductionBatch batch = batchRepo.findById(sc.getBatch().getId())
                .orElseThrow(() -> new BusinessException("批次不存在"));

        return buildTraceResult(sc, batch);
    }

    @Transactional(readOnly = true)
    public TraceResult queryByBatchNumber(String batchNumber) {
        ProductionBatch batch = batchRepo.findByBatchNumberAndIsDeletedFalse(batchNumber)
                .orElseThrow(() -> new BusinessException("批次不存在: " + batchNumber));

        return buildTraceResult(null, batch);
    }

    private TraceResult buildTraceResult(SecurityCode sc, ProductionBatch batch) {
        Product product = batch.getProduct();
        if (product == null && batch.getProductId() != null) {
            product = productRepo.findById(batch.getProductId()).orElse(null);
        }

        List<MaterialInfo> materials = buildMaterialInfos(batch.getId());

        List<Inspection> inspections = inspectionRepo.findByBatch_Id(batch.getId());
        Inspection inspection = inspections.isEmpty() ? null : inspections.get(0);

        Storage storage = batch.getStorageId() != null
                ? storageRepo.findById(batch.getStorageId()).orElse(null)
                : null;

        TransportSale transportSale = batch.getTransportSaleId() != null
                ? transportSaleRepo.findById(batch.getTransportSaleId()).orElse(null)
                : null;

        String status = sc != null ? sc.getStatus() : "未扫码";
        Boolean isRepeatedQuery = sc != null && sc.isRepeatedQuery();
        int scanCount = sc != null ? sc.getQueryCount() : 0;
        String firstScanTime = sc != null && sc.getFirstScanTime() != null
                ? sc.getFirstScanTime().toString()
                : null;

        return new TraceResult(product, batch, materials, inspection, storage, transportSale,
                status, isRepeatedQuery, scanCount, firstScanTime);
    }

    private List<MaterialInfo> buildMaterialInfos(Long batchId) {
        List<BatchMaterialRelation> relations = relationRepo.findById_BatchId(batchId);
        return relations.stream()
                .map(r -> {
                    MaterialPurchase mp = materialPurchaseRepo
                            .findById(r.getId().getMaterialPurchaseId()).orElse(null);
                    if (mp == null) return new MaterialInfo(null, null, null, null);
                    String materialName = mp.getMaterial() != null ? mp.getMaterial().getName() : null;
                    return new MaterialInfo(materialName, mp.getBatchNumber(),
                            mp.getSupplierName(), mp.getProducerName());
                })
                .toList();
    }

    public record MaterialInfo(String materialName, String batchNumber,
                                String supplierName, String producerName) {}

    public record TraceResult(Product product, ProductionBatch batch,
                                List<MaterialInfo> materials,
                                Inspection inspection, Storage storage,
                                TransportSale transportSale,
                                String status, Boolean isRepeatedQuery,
                                int scanCount, String firstScanTime) {}
}
