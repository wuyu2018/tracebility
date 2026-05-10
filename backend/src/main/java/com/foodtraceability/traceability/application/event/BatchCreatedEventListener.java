package com.foodtraceability.traceability.application.event;

import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.service.BlockchainService;
import com.foodtraceability.traceability.domain.event.BatchCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BatchCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(BatchCreatedEventListener.class);

    private final ProductionBatchRepository batchRepo;
    private final BlockchainService blockchainService;

    public BatchCreatedEventListener(ProductionBatchRepository batchRepo,
                                      BlockchainService blockchainService) {
        this.batchRepo = batchRepo;
        this.blockchainService = blockchainService;
    }

    @TransactionalEventListener
    public void onBatchCreated(BatchCreated event) {
        log.info("[Event] BatchCreated: id={}, batchNumber={}, productId={}, materials={}",
                event.batchId(), event.batchNumber(), event.productId(), event.materialPurchaseIds());

        batchRepo.findById(event.batchId()).ifPresent(batch -> {
            String snapshot = String.format(
                    "{\"batchId\":%d,\"batchNumber\":\"%s\",\"productId\":%d,\"productionDate\":\"%s\",\"shelfLife\":\"%s\",\"quantity\":%.2f,\"unit\":\"%s\"}",
                    batch.getId(), batch.getBatchNumber(), batch.getProductId(),
                    batch.getProductionDate(), batch.getShelfLife() != null ? batch.getShelfLife() : "",
                    batch.getQuantity() != null ? batch.getQuantity() : 0.0,
                    batch.getUnit() != null ? batch.getUnit() : "");
            blockchainService.appendBatchChainBlock(
                    batch.getId(), "PRODUCTION_BATCH", batch.getId(), "CREATE",
                    snapshot, null);
            log.info("[Blockchain] Batch chain genesis block created for batchId={}", batch.getId());
        });
    }
}
