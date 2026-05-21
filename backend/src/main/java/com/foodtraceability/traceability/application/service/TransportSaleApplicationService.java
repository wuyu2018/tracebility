package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.ProductionBatch;
import com.foodtraceability.entity.TransportSale;
import com.foodtraceability.exception.BusinessException;
import com.foodtraceability.repository.ProductionBatchRepository;
import com.foodtraceability.repository.TransportSaleRepository;
import com.foodtraceability.traceability.domain.event.TransportSaleRecorded;
import com.foodtraceability.traceability.infrastructure.messaging.DomainEventPublisherImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
@Transactional
public class TransportSaleApplicationService {

    private static final Logger log = LoggerFactory.getLogger(TransportSaleApplicationService.class);

    private final TransportSaleRepository transportSaleRepo;
    private final ProductionBatchRepository batchRepo;
    private final DomainEventPublisherImpl eventPublisher;

    public TransportSaleApplicationService(TransportSaleRepository transportSaleRepo,
                                            ProductionBatchRepository batchRepo,
                                            DomainEventPublisherImpl eventPublisher) {
        this.transportSaleRepo = transportSaleRepo;
        this.batchRepo = batchRepo;
        this.eventPublisher = eventPublisher;
    }

    public record RecordTransportSaleRequest(Long batchId, Double environmentTemperature,
                                              Double productTemperature, LocalDateTime time,
                                              String transportCompany, String vehicleNumber,
                                              String salesRegion, String receiverName,
                                              String receiverContact,
                                              String recorderName, Long companyId) {}

    public record RecordTransportSaleResponse(Long id, Long batchId, String transportCompany, String salesRegion) {}

    public RecordTransportSaleResponse recordTransportSale(RecordTransportSaleRequest req) {
        ProductionBatch batch = batchRepo.findById(req.batchId())
                .orElseThrow(() -> new BusinessException("批次不存在: " + req.batchId()));

        TransportSale ts = new TransportSale();
        ts.associateBatch(batch);
        ts.setEnvironmentTemperature(req.environmentTemperature());
        ts.setProductTemperature(req.productTemperature());
        ts.setTime(LocalDateTime.now());
        ts.setRecorderName(req.recorderName());
        ts.setTransportCompany(req.transportCompany());
        ts.setVehicleNumber(req.vehicleNumber());
        ts.setSalesRegion(req.salesRegion());
        ts.setReceiverName(req.receiverName());
        ts.setReceiverContact(req.receiverContact());
        if (req.companyId() != null) {
            ts.setCompanyId(req.companyId());
        }
        ts = transportSaleRepo.save(ts);

        Long tsId = ts.getId();
        batch.associateTransportSale(ts);
        batchRepo.save(batch);

        var event = new TransportSaleRecorded(tsId, req.batchId(), req.time(),
                req.transportCompany(), req.salesRegion());
        publishAfterCommit(event);

        log.info("TransportSale recorded: id={}, batchId={}", tsId, req.batchId());
        return new RecordTransportSaleResponse(tsId, req.batchId(), req.transportCompany(), req.salesRegion());
    }

    private void publishAfterCommit(TransportSaleRecorded event) {
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
