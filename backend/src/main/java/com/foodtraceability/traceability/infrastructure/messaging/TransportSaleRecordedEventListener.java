package com.foodtraceability.traceability.infrastructure.messaging;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.traceability.domain.event.TransportSaleRecorded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransportSaleRecordedEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(TransportSaleRecordedEventListener.class);
    
    private final MultiAgentCoordinator agentCoordinator;
    
    @Autowired
    public TransportSaleRecordedEventListener(MultiAgentCoordinator agentCoordinator) {
        this.agentCoordinator = agentCoordinator;
    }
    
    @TransactionalEventListener
    public void handleTransportSaleRecorded(TransportSaleRecorded event) {
        log.info("Handling TransportSaleRecorded event: saleId={}, batchId={}",
                event.transportSaleId(), event.batchId());

        try {
            var circulationAgent = agentCoordinator.getCirculationAgent();
            var salesAgent = agentCoordinator.getSalesAgent();

            if (!circulationAgent.isAuthorized() || !salesAgent.isAuthorized()) {
                log.error("Agent not authorized");
                throw new IllegalStateException("Agent not authorized");
            }

            circulationAgent.recordTransport(
                event.batchId().toString(),
                event.transportCompany(),
                event.salesRegion()
            );

            circulationAgent.updateCreditForTimeliness(true);

            salesAgent.recordSale(
                event.batchId().toString(),
                "TRC-" + event.batchId(),
                0L
            );

            log.info("Transport and sale recorded successfully");

        } catch (Exception e) {
            log.error("Failed to handle TransportSaleRecorded event", e);
            throw e;
        }
    }
}
