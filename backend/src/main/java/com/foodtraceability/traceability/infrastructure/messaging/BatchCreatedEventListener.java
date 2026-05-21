package com.foodtraceability.traceability.infrastructure.messaging;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.traceability.domain.event.BatchCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BatchCreatedEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(BatchCreatedEventListener.class);
    
    private final MultiAgentCoordinator agentCoordinator;
    
    @Autowired
    public BatchCreatedEventListener(MultiAgentCoordinator agentCoordinator) {
        this.agentCoordinator = agentCoordinator;
    }
    
    @TransactionalEventListener
    public void handleBatchCreated(BatchCreated event) {
        log.info("Handling BatchCreated event: batchId={}, productId={}", 
                event.batchId(), event.productId());
        
        try {
            var productionAgent = agentCoordinator.getProductionAgent();
            
            if (!productionAgent.isAuthorized()) {
                log.error("Production agent not authorized");
                throw new IllegalStateException("Production agent not authorized");
            }
            
            productionAgent.recordProductionBatch(event.batchId(), String.valueOf(event.productId()));
            
            String traceabilityCode = productionAgent.generateTraceabilityCode(event.batchId());
            productionAgent.addMetadata("batch_" + event.batchId(), traceabilityCode);
            
            productionAgent.updateCreditScore(5);
            
            log.info("Batch created with traceability code: {}", traceabilityCode);
            
        } catch (Exception e) {
            log.error("Failed to handle BatchCreated event", e);
            throw e;
        }
    }
}
