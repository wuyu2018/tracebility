package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.*;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        ProductDto productDto = product != null
                ? new ProductDto(product.getId(), product.getName(), product.getSpecification(),
                        product.getShelfLife(), product.getImageUrl(),
                        product.getContactPhone(), product.getContactEmail())
                : null;

        BatchDto batchDto = new BatchDto(batch.getId(), batch.getBatchNumber(),
                batch.getProductionDate(), batch.getShelfLife(), batch.getCreatedAt());

        List<MaterialInfo> materials = buildMaterialInfos(batch.getId());

        List<Inspection> inspections = inspectionRepo.findByBatch_Id(batch.getId());
        Inspection inspection = inspections.isEmpty() ? null : inspections.get(0);
        InspectionDto inspectionDto = inspection != null
                ? new InspectionDto(inspection.getSampleName(), inspection.getSampleQuantity(),
                        inspection.getSampleSpecification(), inspection.getImageUrl(),
                        inspection.getInspectorName(), inspection.getInspectionTime(),
                        inspection.getResultStatus(), inspection.getResultDetail())
                : null;

        Storage storage = batch.getStorageId() != null
                ? storageRepo.findById(batch.getStorageId()).orElse(null)
                : null;
        StorageDto storageDto = storage != null
                ? new StorageDto(storage.getStorageTime(), storage.getOutboundTime(),
                        storage.getWarehouseLocation())
                : null;

        TransportSale transportSale = batch.getTransportSaleId() != null
                ? transportSaleRepo.findById(batch.getTransportSaleId()).orElse(null)
                : null;
        TransportSaleDto transportSaleDto = transportSale != null
                ? new TransportSaleDto(transportSale.getTime(),
                        transportSale.getSalesRegion(), transportSale.getTransportCompany())
                : null;

        String status = sc != null ? sc.getStatus() : "未扫码";
        Boolean isRepeatedQuery = sc != null && sc.isRepeatedQuery();
        int scanCount = sc != null ? sc.getQueryCount() : 0;
        String firstScanTime = sc != null && sc.getFirstScanTime() != null
                ? sc.getFirstScanTime().toString()
                : null;

        return new TraceResult(productDto, batchDto, materials, inspectionDto, storageDto,
                transportSaleDto, status, isRepeatedQuery, scanCount, firstScanTime);
    }

    private List<MaterialInfo> buildMaterialInfos(Long batchId) {
        List<BatchMaterialRelation> relations = relationRepo.findById_BatchId(batchId);
        return relations.stream()
                .map(r -> {
                    MaterialPurchase mp = materialPurchaseRepo
                            .findById(r.getId().getMaterialPurchaseId()).orElse(null);
                    if (mp == null) return new MaterialInfo(null, null, null, null, null, null);
                    String materialName = mp.getMaterial() != null ? mp.getMaterial().getName() : null;
                    return new MaterialInfo(materialName, mp.getBatchNumber(),
                            mp.getSupplierName(), mp.getProducerName(),
                            mp.getProducerAddress(), mp.getPurchaseDate());
                })
                .toList();
    }

    public record ProductDto(Long id, String name, String specification,
                              String shelfLife, String imageUrl,
                              String contactPhone, String contactEmail) {}

    public record BatchDto(Long id, String batchNumber, LocalDate productionDate,
                            String shelfLife, LocalDateTime createdAt) {}

    public record InspectionDto(String sampleName, Integer sampleQuantity,
                                 String sampleSpecification, String imageUrl,
                                 String inspectorName, LocalDateTime inspectionTime,
                                 String resultStatus, String resultDetail) {}

    public record StorageDto(LocalDateTime storageTime, LocalDateTime outboundTime,
                              String warehouseLocation) {}

    public record TransportSaleDto(LocalDateTime time, String salesRegion,
                                    String transportCompany) {}

    public record MaterialInfo(String materialName, String batchNumber,
                                String supplierName, String producerName,
                                String producerAddress, LocalDateTime purchaseDate) {}

    public record TraceResult(ProductDto product, BatchDto batch,
                                List<MaterialInfo> materials,
                                InspectionDto inspection, StorageDto storage,
                                TransportSaleDto transportSale,
                                String status, Boolean isRepeatedQuery,
                                int scanCount, String firstScanTime) {}
}
