package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.BatchMaterialRelation;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.BatchMaterialRelationRepository;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.traceability.application.dto.CreateBatchRequest;
import com.foodtraceability.traceability.application.dto.CreateBatchResponse;
import com.foodtraceability.traceability.domain.event.BatchCreated;
import com.foodtraceability.traceability.domain.service.BatchCreationValidator;
import com.foodtraceability.traceability.domain.vo.BatchNumber;
import com.foodtraceability.traceability.domain.vo.Quantity;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class ProductionBatchApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ProductionBatchApplicationService.class);

    private final ProductionBatchRepository batchRepo;
    private final ProductRepository productRepo;
    private final BatchMaterialRelationRepository relationRepo;
    private final BatchCreationValidator validator;
    private final DomainEventPublisherImpl eventPublisher;

    public ProductionBatchApplicationService(ProductionBatchRepository batchRepo,
                                             ProductRepository productRepo,
                                             BatchMaterialRelationRepository relationRepo,
                                             BatchCreationValidator validator,
                                             DomainEventPublisherImpl eventPublisher) {
        this.batchRepo = batchRepo;
        this.productRepo = productRepo;
        this.relationRepo = relationRepo;
        this.validator = validator;
        this.eventPublisher = eventPublisher;
    }

    private long nextBatchSeq() {
        return batchRepo.findByIsDeletedFalse().stream()
                .map(ProductionBatch::getBatchNumber)
                .filter(n -> n != null && n.matches("B\\d{8}\\d{4}"))
                .map(n -> n.substring(9))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0) + 1;
    }

    public CreateBatchResponse createBatch(CreateBatchRequest req) {
        var product = productRepo.findById(req.productId())
                .orElseThrow(() -> new BusinessException("产品不存在"));
        validator.validateProduct(product);
        validator.validateMaterialsNotEmpty(req.materialPurchaseIds());

        String shelfLife = req.shelfLife() != null ? req.shelfLife() : product.getShelfLife();
        validator.validateShelfLife(shelfLife);

        BatchNumber batchNo = BatchNumber.generate(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                nextBatchSeq());
        Quantity qty = Quantity.of(req.quantity() != null ? req.quantity() : 0.0,
                req.unit() != null ? req.unit() : "");

        ProductionBatch batch = ProductionBatch.create(
                batchNo, req.productId(), req.productionDate(), shelfLife, qty);
        batch = batchRepo.save(batch);

        Long batchId = batch.getId();
        for (Long mpId : req.materialPurchaseIds()) {
            relationRepo.save(BatchMaterialRelation.create(batchId, mpId));
        }

        var event = new BatchCreated(batchId, batchNo, req.productId(), req.materialPurchaseIds());
        publishAfterCommit(event);

        log.info("Batch created: {} (productId={})", batchNo, req.productId());
        return new CreateBatchResponse(batchId, batchNo.value(), product.getName());
    }

    private void publishAfterCommit(BatchCreated event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            eventPublisher.publish(event);
                        }
                    });
        }
    }
}
