package com.foodtraceability.traceability.application.event;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.InspectionRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.traceability.domain.event.InspectionCompleted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InspectionCompletedEventListener {

    private static final Logger log = LoggerFactory.getLogger(InspectionCompletedEventListener.class);

    private final SecurityCodeRepository securityCodeRepo;
    private final InspectionRepository inspectionRepo;
    private final MultiAgentCoordinator agentCoordinator;
    private final AgentBlockchainService agentBlockchainService;

    public InspectionCompletedEventListener(SecurityCodeRepository securityCodeRepo,
                                             InspectionRepository inspectionRepo,
                                             MultiAgentCoordinator agentCoordinator,
                                             AgentBlockchainService agentBlockchainService) {
        this.securityCodeRepo = securityCodeRepo;
        this.inspectionRepo = inspectionRepo;
        this.agentCoordinator = agentCoordinator;
        this.agentBlockchainService = agentBlockchainService;
    }

    @TransactionalEventListener
    public void onInspectionCompleted(InspectionCompleted event) {
        log.info("[Event] InspectionCompleted: inspectionId={}, batchId={}, qualified={}",
                event.inspectionId(), event.batchId(), event.isQualified());

        if (!event.isQualified()) {
            freezeSecurityCodes(event.batchId());
        }

        inspectionRepo.findById(event.inspectionId()).ifPresent(inspection -> {
            String snapshot = String.format(
                    "{\"inspectionId\":%d,\"batchId\":%d,\"sampleName\":\"%s\",\"sampleQuantity\":%d,\"sampleSpecification\":\"%s\",\"resultStatus\":\"%s\",\"resultDetail\":\"%s\",\"inspectorName\":\"%s\",\"inspectionTime\":\"%s\"}",
                    inspection.getId(), inspection.getBatchId(),
                    inspection.getSampleName() != null ? inspection.getSampleName() : "",
                    inspection.getSampleQuantity() != null ? inspection.getSampleQuantity() : 0,
                    inspection.getSampleSpecification() != null ? inspection.getSampleSpecification() : "",
                    inspection.getResultStatus() != null ? inspection.getResultStatus() : "",
                    inspection.getResultDetail() != null ? inspection.getResultDetail() : "",
                    inspection.getInspectorName() != null ? inspection.getInspectorName() : "",
                    inspection.getInspectionTime() != null ? inspection.getInspectionTime().toString() : "");
            try {
                agentBlockchainService.appendBlockWithConsensus(
                        "BATCH", "INSPECTION", inspection.getId(), "CREATE",
                        snapshot, null);
                log.info("[Blockchain] Inspection block appended via agent for batchId={}, inspectionId={}",
                        inspection.getBatchId(), inspection.getId());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for batchId={}, inspectionId={}",
                        inspection.getBatchId(), inspection.getId(), e);
            }
        });

        try {
            var productionAgent = agentCoordinator.getProductionAgent();
            if (productionAgent.isAuthorized()) {
                if (event.isQualified()) {
                    productionAgent.updateCreditForQuality(90);
                    log.info("[Agent] ProductionAgent credit +10 for qualified inspection batchId={}", event.batchId());
                } else {
                    productionAgent.updateCreditForQuality(50);
                    log.warn("[Agent] ProductionAgent credit -20 for failed inspection batchId={}", event.batchId());
                }
            }
        } catch (Exception e) {
            log.error("[Agent] Failed to notify ProductionAgent", e);
        }
    }

    private void freezeSecurityCodes(Long batchId) {
        var codes = securityCodeRepo.findByBatch_Id(batchId);
        for (SecurityCode sc : codes) {
            sc.freeze();
        }
        securityCodeRepo.saveAll(codes);
        log.warn("[Event] 批次不合格，已冻结 {} 个防伪码 (batchId={})", codes.size(), batchId);
    }
}
