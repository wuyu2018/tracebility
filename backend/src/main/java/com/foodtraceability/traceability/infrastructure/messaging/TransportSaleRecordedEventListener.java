package com.foodtraceability.traceability.infrastructure.messaging;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.repository.TransportSaleRepository;
import com.foodtraceability.traceability.domain.event.TransportSaleRecorded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class TransportSaleRecordedEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransportSaleRecordedEventListener.class);

    private final MultiAgentCoordinator agentCoordinator;
    private final AgentBlockchainService agentBlockchainService;
    private final TransportSaleRepository transportSaleRepo;

    @Autowired
    public TransportSaleRecordedEventListener(MultiAgentCoordinator agentCoordinator,
                                               AgentBlockchainService agentBlockchainService,
                                               TransportSaleRepository transportSaleRepo) {
        this.agentCoordinator = agentCoordinator;
        this.agentBlockchainService = agentBlockchainService;
        this.transportSaleRepo = transportSaleRepo;
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

            transportSaleRepo.findById(event.transportSaleId()).ifPresent(ts -> {
                try {
                    Map<String, Object> snapshot = new LinkedHashMap<>();
                    snapshot.put("id", ts.getId());
                    snapshot.put("batchId", ts.getBatchId());
                    snapshot.put("transportCompany", ts.getTransportCompany());
                    snapshot.put("vehicleNumber", ts.getVehicleNumber());
                    snapshot.put("salesRegion", ts.getSalesRegion());
                    snapshot.put("receiverName", ts.getReceiverName());
                    snapshot.put("receiverContact", ts.getReceiverContact());
                    snapshot.put("recorderName", ts.getRecorderName());
                    snapshot.put("environmentTemperature", ts.getEnvironmentTemperature());
                    snapshot.put("productTemperature", ts.getProductTemperature());
                    snapshot.put("time", ts.getTime() != null ? ts.getTime().toString() : null);
                    String snapshotJson = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(snapshot);
                    agentBlockchainService.appendBlockWithConsensus(
                            "BATCH", "TRANSPORT_SALE", ts.getId(), "CREATE",
                            snapshotJson, null);
                    log.info("[Blockchain] TransportSale block appended via agent: saleId={}",
                            ts.getId());
                } catch (Exception e) {
                    log.error("[Blockchain] Failed to append block for transportSaleId={}",
                            ts.getId(), e);
                }
            });

            log.info("Transport and sale recorded successfully");

        } catch (Exception e) {
            log.error("Failed to handle TransportSaleRecorded event", e);
            throw e;
        }
    }
}
