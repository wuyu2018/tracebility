package com.foodtraceability.traceability.application.event;

import com.foodtraceability.entity.TransportSale;
import com.foodtraceability.repository.TransportSaleRepository;
import com.foodtraceability.service.BlockchainService;
import com.foodtraceability.traceability.domain.event.TransportSaleRecorded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TransportSaleRecordedEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransportSaleRecordedEventListener.class);

    private final TransportSaleRepository transportSaleRepo;
    private final BlockchainService blockchainService;

    public TransportSaleRecordedEventListener(TransportSaleRepository transportSaleRepo,
                                               BlockchainService blockchainService) {
        this.transportSaleRepo = transportSaleRepo;
        this.blockchainService = blockchainService;
    }

    @TransactionalEventListener
    public void onTransportSaleRecorded(TransportSaleRecorded event) {
        log.info("[Event] TransportSaleRecorded: id={}, batchId={}", event.transportSaleId(), event.batchId());

        transportSaleRepo.findById(event.transportSaleId()).ifPresent(ts -> {
            String snapshot = String.format(
                    "{\"transportSaleId\":%d,\"batchId\":%d,\"environmentTemperature\":%.2f,\"productTemperature\":%.2f,\"time\":\"%s\",\"transportCompany\":\"%s\",\"vehicleNumber\":\"%s\",\"salesRegion\":\"%s\",\"receiverName\":\"%s\",\"receiverContact\":\"%s\"}",
                    ts.getId(), ts.getBatchId(),
                    ts.getEnvironmentTemperature() != null ? ts.getEnvironmentTemperature() : 0.0,
                    ts.getProductTemperature() != null ? ts.getProductTemperature() : 0.0,
                    ts.getTime() != null ? ts.getTime() : "",
                    ts.getTransportCompany() != null ? ts.getTransportCompany() : "",
                    ts.getVehicleNumber() != null ? ts.getVehicleNumber() : "",
                    ts.getSalesRegion() != null ? ts.getSalesRegion() : "",
                    ts.getReceiverName() != null ? ts.getReceiverName() : "",
                    ts.getReceiverContact() != null ? ts.getReceiverContact() : "");
            blockchainService.appendBatchChainBlock(
                    ts.getBatchId(), "TRANSPORT_SALE", ts.getId(), "CREATE",
                    snapshot, null);
            log.info("[Blockchain] TransportSale block appended for batchId={}, transportSaleId={}",
                    ts.getBatchId(), ts.getId());
        });
    }
}
