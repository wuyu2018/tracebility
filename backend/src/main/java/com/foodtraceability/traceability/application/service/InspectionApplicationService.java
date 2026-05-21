package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.Inspection;
import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.InspectionRepository;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.traceability.application.dto.CompleteInspectionRequest;
import com.foodtraceability.traceability.application.dto.CompleteInspectionResponse;
import com.foodtraceability.traceability.domain.vo.InspectionResult;
import com.foodtraceability.traceability.domain.event.DomainEvent;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional
public class InspectionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(InspectionApplicationService.class);

    private final InspectionRepository inspectionRepo;
    private final ProductionBatchRepository batchRepo;
    private final DomainEventPublisherImpl eventPublisher;

    public InspectionApplicationService(InspectionRepository inspectionRepo,
                                        ProductionBatchRepository batchRepo,
                                        DomainEventPublisherImpl eventPublisher) {
        this.inspectionRepo = inspectionRepo;
        this.batchRepo = batchRepo;
        this.eventPublisher = eventPublisher;
    }

    public CompleteInspectionResponse completeInspection(CompleteInspectionRequest req) {
        ProductionBatch batch = batchRepo.findById(req.batchId())
                .orElseThrow(() -> new BusinessException("批次不存在: " + req.batchId()));

        Inspection inspection = Inspection.create(
                req.batchId(), req.sampleName(), req.sampleQuantity(),
                req.sampleSpecification());
        if (req.companyId() != null) {
            inspection.setCompanyId(req.companyId());
        }
        if (req.imageUrl() != null) {
            inspection.setImageUrl(req.imageUrl());
        }

        InspectionResult result = req.qualified()
                ? InspectionResult.pass()
                : InspectionResult.fail(req.failReason());
        inspection.complete(result, req.inspectorName());

        inspection = inspectionRepo.save(inspection);

        DomainEvent event = inspection.pullEvents().get(0);
        publishAfterCommit(event);

        log.info("Inspection completed: id={}, batchId={}, result={}",
                inspection.getId(), req.batchId(), result.displayStatus());
        return new CompleteInspectionResponse(inspection.getId(), req.batchId(), result.displayStatus());
    }

    private void publishAfterCommit(DomainEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            eventPublisher.publish(event);
                        }
                    });
        }
    }
}
