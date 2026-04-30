package com.foodtraceability.traceability.application.event;

import com.foodtraceability.traceability.domain.event.BatchCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BatchCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(BatchCreatedEventListener.class);

    @TransactionalEventListener
    public void onBatchCreated(BatchCreated event) {
        log.info("[Event] BatchCreated: id={}, batchNumber={}, productId={}, materials={}",
                event.batchId(), event.batchNumber(), event.productId(), event.materialPurchaseIds());
    }
}
