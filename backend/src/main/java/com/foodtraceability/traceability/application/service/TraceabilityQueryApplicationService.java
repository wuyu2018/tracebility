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

    public record TraceResult(
            Product product,
            ProductionBatch batch,
            List<MaterialInfo> materials,
            Inspection inspection,
            Storage storage,
            TransportSale transportSale,
            String status,
            Boolean isRepeatedQuery,
            Integer scanCount,
            String firstScanTime
    ) {
        public record MaterialInfo(String materialName, String batchNumber, String supplierName, String producerName) {}
    }

    @Transactional
    public TraceResult queryByCode(String code) {
        SecurityCode securityCode = securityCodeRepo.findByCode(code)
                .orElseThrow(() -> new BusinessException("防伪码不存在: " + code));

        securityCode.recordQueryAndActivateIfNeeded();
        securityCodeRepo.save(securityCode);

        Long batchId = securityCode.getBatchId();
        ProductionBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new BusinessException("批次不存在: " + batchId));

        Product product = productRepo.findById(batch.getProductId())
                .orElseThrow(() -> new BusinessException("产品不存在: " + batch.getProductId()));

        List<TraceResult.MaterialInfo> materials = buildMaterialInfos(batchId);
        Inspection inspection = findInspection(batchId);
        Storage storage = findStorage(batch.getStorageId());
        TransportSale transportSale = findTransportSale(batch.getTransportSaleId());

        return new TraceResult(
                product, batch, materials, inspection, storage, transportSale,
                securityCode.getStatus(), securityCode.isRepeatedQuery(),
                securityCode.getQueryCount(),
                securityCode.getFirstScanTime() != null ? securityCode.getFirstScanTime().toString() : null
        );
    }

    @Transactional(readOnly = true)
    public TraceResult queryByBatchNumber(String batchNumber) {
        ProductionBatch batch = batchRepo.findByBatchNumberAndIsDeletedFalse(batchNumber)
                .orElseThrow(() -> new BusinessException("批次不存在: " + batchNumber));

        Product product = productRepo.findById(batch.getProductId())
                .orElseThrow(() -> new BusinessException("产品不存在: " + batch.getProductId()));

        List<TraceResult.MaterialInfo> materials = buildMaterialInfos(batch.getId());
        Inspection inspection = findInspection(batch.getId());
        Storage storage = findStorage(batch.getStorageId());
        TransportSale transportSale = findTransportSale(batch.getTransportSaleId());

        return new TraceResult(
                product, batch, materials, inspection, storage, transportSale,
                "未扫码", false, 0, null
        );
    }

    private List<TraceResult.MaterialInfo> buildMaterialInfos(Long batchId) {
        return relationRepo.findById_BatchId(batchId).stream()
                .map(r -> {
                    MaterialPurchase mp = materialPurchaseRepo
                            .findById(r.getId().getMaterialPurchaseId()).orElse(null);
                    if (mp == null) return null;
                    String materialName = mp.getMaterial() != null ? mp.getMaterial().getName() : null;
                    return new TraceResult.MaterialInfo(
                            materialName, mp.getBatchNumber(),
                            mp.getSupplierName(), mp.getProducerName());
                })
                .filter(m -> m != null)
                .toList();
    }

    private Inspection findInspection(Long batchId) {
        List<Inspection> inspections = inspectionRepo.findByBatchId(batchId);
        return inspections.isEmpty() ? null : inspections.get(0);
    }

    private Storage findStorage(Long storageId) {
        if (storageId == null) return null;
        return storageRepo.findById(storageId).orElse(null);
    }

    private TransportSale findTransportSale(Long transportSaleId) {
        if (transportSaleId == null) return null;
        return transportSaleRepo.findById(transportSaleId).orElse(null);
    }
}
