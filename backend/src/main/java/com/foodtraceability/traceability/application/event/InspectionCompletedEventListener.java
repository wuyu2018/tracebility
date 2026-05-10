package com.foodtraceability.traceability.application.event;

import com.foodtraceability.entity.Inspection;
import com.foodtraceability.entity.SecurityCode;
import com.foodtraceability.repository.InspectionRepository;
import com.foodtraceability.repository.SecurityCodeRepository;
import com.foodtraceability.service.BlockchainService;
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
    private final BlockchainService blockchainService;

    public InspectionCompletedEventListener(SecurityCodeRepository securityCodeRepo,
                                             InspectionRepository inspectionRepo,
                                             BlockchainService blockchainService) {
        this.securityCodeRepo = securityCodeRepo;
        this.inspectionRepo = inspectionRepo;
        this.blockchainService = blockchainService;
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
                    "{\"inspectionId\":%d,\"batchId\":%d,\"sampleName\":\"%s\",\"sampleQuantity\":%d,\"sampleSpecification\":\"%s\",\"resultStatus\":\"%s\",\"resultDetail\":\"%s\"}",
                    inspection.getId(), inspection.getBatchId(),
                    inspection.getSampleName() != null ? inspection.getSampleName() : "",
                    inspection.getSampleQuantity() != null ? inspection.getSampleQuantity() : 0,
                    inspection.getSampleSpecification() != null ? inspection.getSampleSpecification() : "",
                    inspection.getResultStatus() != null ? inspection.getResultStatus() : "",
                    inspection.getResultDetail() != null ? inspection.getResultDetail() : "");
            blockchainService.appendBatchChainBlock(
                    inspection.getBatchId(), "INSPECTION", inspection.getId(), "CREATE",
                    snapshot, null);
            log.info("[Blockchain] Inspection block appended for batchId={}, inspectionId={}",
                    inspection.getBatchId(), inspection.getId());
        });
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
