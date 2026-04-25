package com.foodtraceability.service.domain;

import com.foodtraceability.domain.DomainException;
import com.foodtraceability.domain.DomainService;
import com.foodtraceability.domain.valueobject.TraceInfo;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@DomainService
public class TraceabilityDomainService {

    private final SecurityCodeRepository securityCodeRepository;
    private final ProductionBatchRepository batchRepository;
    private final BatchMaterialRelationRepository relationRepository;
    private final InspectionRepository inspectionRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;

    public TraceabilityDomainService(
            SecurityCodeRepository securityCodeRepository,
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

    @Transactional(readOnly = true)
    public TraceInfo buildFullTraceInfo(String code, boolean forAdmin) {
        SecurityCode securityCode = securityCodeRepository.findByCode(code)
                .orElseThrow(() -> new DomainException("防伪码不存在"));

        ProductionBatch batch = securityCode.getBatch();
        return buildTraceInfoFromBatch(batch, securityCode, forAdmin);
    }

    @Transactional(readOnly = true)
    public TraceInfo buildTraceInfoByBatchNumber(String batchNumber, boolean forAdmin) {
        ProductionBatch batch = batchRepository.findByBatchNumberAndIsDeletedFalse(batchNumber)
                .orElseThrow(() -> new DomainException("生产批次不存在"));

        return buildTraceInfoFromBatch(batch, null, forAdmin);
    }

    private TraceInfo buildTraceInfoFromBatch(ProductionBatch batch, SecurityCode securityCode, boolean forAdmin) {
        List<MaterialPurchase> materials = findMaterialsByBatchId(batch.getId());
        Inspection inspection = findInspectionByBatch(batch);
        Storage storage = findStorageByBatch(batch);
        TransportSale transportSale = findTransportSaleByBatch(batch);

        return batch.buildTraceInfo(
            securityCode,
            materials,
            inspection,
            storage,
            transportSale,
            forAdmin
        );
    }

    private List<MaterialPurchase> findMaterialsByBatchId(Long batchId) {
        List<BatchMaterialRelation> relations = relationRepository.findByBatchId(batchId);
        return relations.stream()
                .map(BatchMaterialRelation::getMaterial)
                .toList();
    }

    private Inspection findInspectionByBatch(ProductionBatch batch) {
        return inspectionRepository.findFirstByBatch(batch).orElse(null);
    }

    private Storage findStorageByBatch(ProductionBatch batch) {
        return storageRepository.findFirstByBatch(batch).orElse(null);
    }

    private TransportSale findTransportSaleByBatch(ProductionBatch batch) {
        return transportSaleRepository.findFirstByBatch(batch).orElse(null);
    }
}
