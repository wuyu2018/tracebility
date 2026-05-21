package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.Storage;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.StorageRepository;
import com.foodtraceability.traceability.application.dto.RecordStorageRequest;
import com.foodtraceability.traceability.application.dto.RecordStorageResponse;
import com.foodtraceability.traceability.domain.event.GoodsReceived;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional
public class StorageApplicationService {

    private static final Logger log = LoggerFactory.getLogger(StorageApplicationService.class);

    private final StorageRepository storageRepo;
    private final ProductionBatchRepository batchRepo;
    private final DomainEventPublisherImpl eventPublisher;

    public StorageApplicationService(StorageRepository storageRepo,
                                     ProductionBatchRepository batchRepo,
                                     DomainEventPublisherImpl eventPublisher) {
        this.storageRepo = storageRepo;
        this.batchRepo = batchRepo;
        this.eventPublisher = eventPublisher;
    }

    public RecordStorageResponse recordStorage(RecordStorageRequest req) {
        ProductionBatch batch = batchRepo.findById(req.batchId())
                .orElseThrow(() -> new BusinessException("批次不存在: " + req.batchId()));

        Storage storage = Storage.create(
                req.batchId(), req.storageTime(), req.quantity(), req.unit(), req.warehouseLocation());
        if (req.companyId() != null) {
            storage.setCompanyId(req.companyId());
        }
        storage = storageRepo.save(storage);

        Long storageId = storage.getId();
        batch.setStorageId(storageId);
        batchRepo.save(batch);

        var event = new GoodsReceived(storageId, req.batchId(), req.storageTime());
        publishAfterCommit(event);

        log.info("Storage recorded: id={}, batchId={}", storageId, req.batchId());
        return new RecordStorageResponse(storageId, req.batchId(), req.warehouseLocation());
    }

    private void publishAfterCommit(GoodsReceived event) {
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
