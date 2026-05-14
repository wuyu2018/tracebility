package com.foodtraceability.traceability.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.repository.MaterialPurchaseRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.service.BlockchainService;
import com.foodtraceability.traceability.domain.event.MaterialPurchaseChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MaterialPurchaseChangedEventListener {

    private static final Logger log = LoggerFactory.getLogger(MaterialPurchaseChangedEventListener.class);

    private final MaterialPurchaseRepository repository;
    private final BlockchainService blockchainService;
    private final BlockchainRetryService blockchainRetryService;
    private final ObjectMapper objectMapper;

    public MaterialPurchaseChangedEventListener(MaterialPurchaseRepository repository,
                                                  BlockchainService blockchainService,
                                                  BlockchainRetryService blockchainRetryService) {
        this.repository = repository;
        this.blockchainService = blockchainService;
        this.blockchainRetryService = blockchainRetryService;
        this.objectMapper = new ObjectMapper();
    }

    @TransactionalEventListener
    public void onMaterialPurchaseChanged(MaterialPurchaseChanged event) {
        log.info("[Event] MaterialPurchaseChanged: id={}, action={}", event.purchaseId(), event.action());

        repository.findById(event.purchaseId()).ifPresent(purchase -> {
            String snapshotJson;
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", purchase.getId());
                snapshot.put("materialId", purchase.getMaterial().getId());
                snapshot.put("materialName", purchase.getMaterialName());
                snapshot.put("batchNumber", purchase.getBatchNumber());
                snapshot.put("supplierName", purchase.getSupplierName());
                snapshot.put("producerName", purchase.getProducerName());
                snapshot.put("producerAddress", purchase.getProducerAddress());
                snapshot.put("purchaseDate", purchase.getPurchaseDate() != null
                        ? purchase.getPurchaseDate().toString() : null);
                snapshot.put("quantity", purchase.getQuantity());
                snapshot.put("unit", purchase.getUnit());
                snapshot.put("is_deleted", purchase.isDeleted());
                snapshotJson = objectMapper.writeValueAsString(snapshot);
            } catch (Exception e) {
                log.error("[Blockchain] Failed to build snapshot for MaterialPurchase id={}",
                        purchase.getId(), e);
                return;
            }

            try {
                blockchainService.appendMaterialChainBlock(
                        "MATERIAL_PURCHASE", purchase.getId(), event.action(),
                        snapshotJson, null);
                log.info("[Blockchain] MaterialPurchase block appended: id={}, action={}",
                        purchase.getId(), event.action());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for MaterialPurchase id={}, action={}",
                        purchase.getId(), event.action(), e);
                blockchainRetryService.scheduleRetry(
                        "MATERIAL", null, "MATERIAL_PURCHASE", purchase.getId(), event.action(),
                        snapshotJson, null, "MaterialPurchaseChanged", e.getMessage());
            }
        });
    }
}
