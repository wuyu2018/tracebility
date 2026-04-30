package com.foodtraceability.traceability.application.event;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.traceability.domain.event.GoodsReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class GoodsReceivedEventListener {

    private static final Logger log = LoggerFactory.getLogger(GoodsReceivedEventListener.class);

    private final ProductionBatchRepository batchRepo;

    public GoodsReceivedEventListener(ProductionBatchRepository batchRepo) {
        this.batchRepo = batchRepo;
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
    }
}
