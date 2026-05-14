package com.foodtraceability.traceability.application.event;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.Storage;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.StorageRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.service.BlockchainService;
import com.foodtraceability.traceability.domain.event.GoodsReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class GoodsReceivedEventListener {

    private static final Logger log = LoggerFactory.getLogger(GoodsReceivedEventListener.class);

    private final ProductionBatchRepository batchRepo;
    private final StorageRepository storageRepo;
    private final BlockchainService blockchainService;
    private final BlockchainRetryService blockchainRetryService;

    public GoodsReceivedEventListener(ProductionBatchRepository batchRepo,
                                       StorageRepository storageRepo,
                                       BlockchainService blockchainService,
                                       BlockchainRetryService blockchainRetryService) {
        this.batchRepo = batchRepo;
        this.storageRepo = storageRepo;
        this.blockchainService = blockchainService;
        this.blockchainRetryService = blockchainRetryService;
    }

    @TransactionalEventListener
    public void onGoodsReceived(GoodsReceived event) {
        log.info("[Event] GoodsReceived: storageId={}, batchId={}", event.storageId(), event.batchId());
        batchRepo.findById(event.batchId()).ifPresent(batch -> {
            batch.setStorageId(event.storageId());
            batchRepo.save(batch);
            log.debug("[Event] ProductionBatch.storageId updated: batchId={}, storageId={}",
                    event.batchId(), event.storageId());
        });

        storageRepo.findById(event.storageId()).ifPresent(storage -> {
            String snapshot = String.format(
                    "{\"storageId\":%d,\"batchId\":%d,\"storageTime\":\"%s\",\"outboundTime\":\"%s\",\"quantity\":%.2f,\"unit\":\"%s\",\"warehouseLocation\":\"%s\"}",
                    storage.getId(), storage.getBatchId(),
                    storage.getStorageTime() != null ? storage.getStorageTime() : "",
                    storage.getOutboundTime() != null ? storage.getOutboundTime() : "",
                    storage.getQuantity() != null ? storage.getQuantity() : 0.0,
                    storage.getUnit() != null ? storage.getUnit() : "",
                    storage.getWarehouseLocation() != null ? storage.getWarehouseLocation() : "");
            try {
                blockchainService.appendBatchChainBlock(
                        storage.getBatchId(), "STORAGE", storage.getId(), "CREATE",
                        snapshot, null);
                log.info("[Blockchain] Storage block appended for batchId={}, storageId={}",
                        storage.getBatchId(), storage.getId());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for batchId={}, storageId={}",
                        storage.getBatchId(), storage.getId(), e);
                blockchainRetryService.scheduleRetry(
                        "BATCH", storage.getBatchId(), "STORAGE", storage.getId(), "CREATE",
                        snapshot, null, "GoodsReceived", e.getMessage());
            }
        });
    }
}
