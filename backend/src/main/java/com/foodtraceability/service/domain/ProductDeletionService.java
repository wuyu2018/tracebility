package com.foodtraceability.service.domain;

import com.foodtraceability.domain.DomainEventPublisher;
import com.foodtraceability.domain.DomainService;
import com.foodtraceability.domain.DeletionResult;
import com.foodtraceability.domain.event.ProductDeletedEvent;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@DomainService
public class ProductDeletionService {

    private static final Logger log = LoggerFactory.getLogger(ProductDeletionService.class);

    private final ProductRepository productRepository;
    private final ProductionBatchRepository batchRepository;
    private final MaterialPurchaseRepository materialPurchaseRepository;
    private final BatchMaterialRelationRepository relationRepository;
    private final SecurityCodeRepository securityCodeRepository;
    private final InspectionRepository inspectionRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;
    private final DomainEventPublisher eventPublisher;

    public ProductDeletionService(
            ProductRepository productRepository,
            ProductionBatchRepository batchRepository,
            MaterialPurchaseRepository materialPurchaseRepository,
            BatchMaterialRelationRepository relationRepository,
            SecurityCodeRepository securityCodeRepository,
            InspectionRepository inspectionRepository,
            StorageRepository storageRepository,
            TransportSaleRepository transportSaleRepository,
            DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.materialPurchaseRepository = materialPurchaseRepository;
        this.relationRepository = relationRepository;
        this.securityCodeRepository = securityCodeRepository;
        this.inspectionRepository = inspectionRepository;
        this.storageRepository = storageRepository;
        this.transportSaleRepository = transportSaleRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DeletionResult deleteProduct(Product product) {
        Long productId = product.getId();
        log.info("[产品删除] 开始删除产品 ID: {}", productId);

        List<ProductionBatch> batches = batchRepository.findByProductIdAndIsDeletedFalse(productId);

        deleteRelatedMaterialPurchases(productId);
        deleteRelatedBatchMaterialRelations(batches);
        deleteRelatedSecurityCodes(batches);
        deleteRelatedInspections(batches);
        deleteRelatedStorages(batches);
        deleteRelatedTransportSales(batches);
        batchRepository.deleteAll(batches);
        productRepository.delete(product);

        Long[] batchIds = batches.stream().map(ProductionBatch::getId).toArray(Long[]::new);
        DeletionResult result = DeletionResult.create(productId, batchIds);

        eventPublisher.publish(new ProductDeletedEvent(productId, batchIds));

        log.info("[产品删除] 删除完成，影响 {} 个批次", batchIds.length);
        return result;
    }

    private void deleteRelatedMaterialPurchases(Long productId) {
        List<MaterialPurchase> materials = materialPurchaseRepository.findByProductIdAndIsDeletedFalse(productId);
        materialPurchaseRepository.deleteAll(materials);
    }

    private void deleteRelatedBatchMaterialRelations(List<ProductionBatch> batches) {
        for (ProductionBatch batch : batches) {
            List<BatchMaterialRelation> relations = relationRepository.findByBatchId(batch.getId());
            relationRepository.deleteAll(relations);
        }
    }

    private void deleteRelatedSecurityCodes(List<ProductionBatch> batches) {
        for (ProductionBatch batch : batches) {
            List<SecurityCode> codes = securityCodeRepository.findByBatchId(batch.getId());
            securityCodeRepository.deleteAll(codes);
        }
    }

    private void deleteRelatedInspections(List<ProductionBatch> batches) {
        for (ProductionBatch batch : batches) {
            List<Inspection> inspections = inspectionRepository.findByBatch(batch);
            inspectionRepository.deleteAll(inspections);
        }
    }

    private void deleteRelatedStorages(List<ProductionBatch> batches) {
        for (ProductionBatch batch : batches) {
            List<Storage> storages = storageRepository.findByBatch(batch);
            storageRepository.deleteAll(storages);
        }
    }

    private void deleteRelatedTransportSales(List<ProductionBatch> batches) {
        for (ProductionBatch batch : batches) {
            List<TransportSale> transportSales = transportSaleRepository.findByBatch(batch);
            transportSaleRepository.deleteAll(transportSales);
        }
    }
}
