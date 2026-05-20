package com.foodtraceability.traceability.infrastructure.messaging;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.traceability.domain.event.MaterialChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MaterialChangedEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(MaterialChangedEventListener.class);
    
    private final MultiAgentCoordinator agentCoordinator;
    
    @Autowired
    public MaterialChangedEventListener(MultiAgentCoordinator agentCoordinator) {
        this.agentCoordinator = agentCoordinator;
    }
    
    @TransactionalEventListener
    public void handleMaterialChanged(MaterialChanged event) {
        log.info("Handling MaterialChanged event: materialId={}, action={}", 
                event.materialId(), event.action());
        
        try {
            switch (event.action()) {
                case "CREATED":
                    handleMaterialCreation(event);
                    break;
                case "UPDATED":
                    handleMaterialUpdate(event);
                    break;
                case "DELETED":
                    handleMaterialDeletion(event);
                    break;
                default:
                    log.warn("Unknown material action: {}", event.action());
            }
        } catch (Exception e) {
            log.error("Failed to handle MaterialChanged event", e);
            throw e;
        }
    }
    
    private void handleMaterialCreation(MaterialChanged event) {
        var productionAgent = agentCoordinator.getProductionAgent();
        
        if (!productionAgent.isAuthorized()) {
            log.error("Production agent not authorized");
            throw new IllegalStateException("Production agent not authorized");
        }
        
        String traceabilityCode = productionAgent.generateTraceabilityCode(event.materialId());
        productionAgent.addMetadata("material_" + event.materialId(), traceabilityCode);
        
        log.info("Material created with traceability code: {}", traceabilityCode);
    }
    
    private void handleMaterialUpdate(MaterialChanged event) {
        var productionAgent = agentCoordinator.getProductionAgent();
        productionAgent.updateCreditScore(2);
        
        log.info("Material updated, credit score adjusted");
    }
    
    private void handleMaterialDeletion(MaterialChanged event) {
        var productionAgent = agentCoordinator.getProductionAgent();
        productionAgent.updateCreditScore(-5);
        
        log.info("Material deleted, credit score adjusted");
    }
}
