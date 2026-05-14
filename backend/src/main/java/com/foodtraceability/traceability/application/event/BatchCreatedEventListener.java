package com.foodtraceability.traceability.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.service.BlockchainService;
import com.foodtraceability.traceability.domain.event.BatchCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BatchCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(BatchCreatedEventListener.class);

    private final ProductionBatchRepository batchRepo;
    private final BlockchainService blockchainService;
    private final BlockchainRetryService blockchainRetryService;
    private final ObjectMapper objectMapper;

    public BatchCreatedEventListener(ProductionBatchRepository batchRepo,
                                      BlockchainService blockchainService,
                                      BlockchainRetryService blockchainRetryService) {
        this.batchRepo = batchRepo;
        this.blockchainService = blockchainService;
        this.blockchainRetryService = blockchainRetryService;
        this.objectMapper = new ObjectMapper();
    }

    @TransactionalEventListener
    public void onBatchCreated(BatchCreated event) {
        log.info("[Event] BatchCreated: id={}, batchNumber={}, productId={}, materials={}",
                event.batchId(), event.batchNumber(), event.productId(), event.materialPurchaseIds());

        batchRepo.findById(event.batchId()).ifPresent(batch -> {
            String snapshotJson;
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", batch.getId());
                snapshot.put("batchNumber", batch.getBatchNumber());
                snapshot.put("productId", batch.getProductId());
                snapshot.put("productionDate", batch.getProductionDate() != null ? batch.getProductionDate().toString() : null);
                snapshot.put("shelfLife", batch.getShelfLife());
                snapshot.put("quantity", batch.getQuantity());
                snapshot.put("unit", batch.getUnit());
                snapshot.put("storageId", batch.getStorageId());
                snapshot.put("transportSaleId", batch.getTransportSaleId());
                snapshot.put("isDeleted", batch.isDeleted());
                snapshot.put("createdAt", batch.getCreatedAt() != null ? batch.getCreatedAt().toString() : null);
                snapshotJson = objectMapper.writeValueAsString(snapshot);
            } catch (Exception e) {
                log.error("[Blockchain] Failed to build snapshot for batchId={}", batch.getId(), e);
                return;
            }

            try {
                blockchainService.appendBatchChainBlock(
                        batch.getId(), "PRODUCTION_BATCH", batch.getId(), "CREATE",
                        snapshotJson, null);
                log.info("[Blockchain] Batch chain genesis block created for batchId={}", batch.getId());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for batchId={}", batch.getId(), e);
                blockchainRetryService.scheduleRetry(
                        "BATCH", batch.getId(), "PRODUCTION_BATCH", batch.getId(), "CREATE",
                        snapshotJson, null, "BatchCreated", e.getMessage());
            }
        });
    }
}
