package com.foodtraceability.traceability.infrastructure.messaging;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.traceability.domain.event.BatchCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class BatchCreatedEventListener {

    private static final Logger log = LoggerFactory.getLogger(BatchCreatedEventListener.class);

    private final MultiAgentCoordinator agentCoordinator;
    private final AgentBlockchainService agentBlockchainService;
    private final BlockchainRetryService blockchainRetryService;
    private final ProductionBatchRepository batchRepo;

    @Autowired
    public BatchCreatedEventListener(MultiAgentCoordinator agentCoordinator,
                                      AgentBlockchainService agentBlockchainService,
                                      BlockchainRetryService blockchainRetryService,
                                      ProductionBatchRepository batchRepo) {
        this.agentCoordinator = agentCoordinator;
        this.agentBlockchainService = agentBlockchainService;
        this.blockchainRetryService = blockchainRetryService;
        this.batchRepo = batchRepo;
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

            batchRepo.findById(event.batchId()).ifPresent(batch -> {
                String snapshotJson = null;
                try {
                    Map<String, Object> snapshot = new LinkedHashMap<>();
                    snapshot.put("id", batch.getId());
                    snapshot.put("batchNumber", batch.getBatchNumber());
                    snapshot.put("productId", batch.getProductId());
                    snapshot.put("productionDate", batch.getProductionDate() != null
                            ? batch.getProductionDate().toString() : null);
                    snapshot.put("shelfLife", batch.getShelfLife());
                    snapshot.put("quantity", batch.getQuantity());
                    snapshot.put("unit", batch.getUnit());
                    snapshotJson = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(snapshot);
                    agentBlockchainService.appendBlockWithConsensus(
                            "BATCH", "PRODUCTION_BATCH", batch.getId(), "CREATE",
                            snapshotJson, null);
                    log.info("[Blockchain] ProductionBatch block appended via agent: batchId={}",
                            batch.getId());
                } catch (Exception e) {
                    log.error("[Blockchain] Failed to append block for batchId={} — scheduling retry",
                            batch.getId(), e);
                    blockchainRetryService.scheduleRetry(
                            "BATCH", "PRODUCTION_BATCH", batch.getId(), "CREATE",
                            snapshotJson, null, null, e.getMessage());
                }
            });

            log.info("Batch created with traceability code: {}", traceabilityCode);

        } catch (Exception e) {
            log.error("Failed to handle BatchCreated event", e);
            throw e;
        }
    }
}
