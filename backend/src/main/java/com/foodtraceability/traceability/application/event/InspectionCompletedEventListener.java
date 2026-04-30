package com.foodtraceability.traceability.application.event;

import com.foodtraceability.entity.SecurityCode;
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

    public InspectionCompletedEventListener(SecurityCodeRepository securityCodeRepo) {
        this.securityCodeRepo = securityCodeRepo;
    }

    @TransactionalEventListener
    public void onInspectionCompleted(InspectionCompleted event) {
        log.info("[Event] InspectionCompleted: inspectionId={}, batchId={}, qualified={}",
                event.inspectionId(), event.batchId(), event.isQualified());

        if (!event.isQualified()) {
            freezeSecurityCodes(event.batchId());
        }
    }

    private void freezeSecurityCodes(Long batchId) {
        var codes = securityCodeRepo.findByBatch_Id(batchId);
        for (SecurityCode sc : codes) {
            sc.setStatus(SecurityCode.STATUS_FROZEN);
        }
        securityCodeRepo.saveAll(codes);
        log.warn("[Event] 批次不合格，已冻结 {} 个防伪码 (batchId={})", codes.size(), batchId);
    }
}
