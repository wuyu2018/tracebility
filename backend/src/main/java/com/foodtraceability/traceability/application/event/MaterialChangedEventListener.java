package com.foodtraceability.traceability.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.repository.MaterialRepository;
import com.foodtraceability.service.BlockchainRetryService;
import com.foodtraceability.service.BlockchainService;
import com.foodtraceability.traceability.domain.event.MaterialChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MaterialChangedEventListener {

    private static final Logger log = LoggerFactory.getLogger(MaterialChangedEventListener.class);

    private final MaterialRepository repository;
    private final BlockchainService blockchainService;
    private final BlockchainRetryService blockchainRetryService;
    private final ObjectMapper objectMapper;

    public MaterialChangedEventListener(MaterialRepository repository,
                                         BlockchainService blockchainService,
                                         BlockchainRetryService blockchainRetryService) {
        this.repository = repository;
        this.blockchainService = blockchainService;
        this.blockchainRetryService = blockchainRetryService;
        this.objectMapper = new ObjectMapper();
    }

    @TransactionalEventListener
    public void onMaterialChanged(MaterialChanged event) {
        log.info("[Event] MaterialChanged: id={}, action={}", event.materialId(), event.action());

        repository.findById(event.materialId()).ifPresent(material -> {
            String snapshotJson;
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", material.getId());
                snapshot.put("name", material.getName());
                snapshot.put("isActive", material.isActive());

                if ("DEACTIVATE".equals(event.action())) {
                    snapshot.put("is_deleted", true);
                }
                snapshotJson = objectMapper.writeValueAsString(snapshot);
            } catch (Exception e) {
                log.error("[Blockchain] Failed to build snapshot for Material id={}", material.getId(), e);
                return;
            }

            try {
                blockchainService.appendMaterialChainBlock(
                        "MATERIAL", material.getId(), event.action(),
                        snapshotJson, null);
                log.info("[Blockchain] Material block appended: id={}, action={}",
                        material.getId(), event.action());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for Material id={}, action={}",
                        material.getId(), event.action(), e);
                blockchainRetryService.scheduleRetry(
                        "MATERIAL", null, "MATERIAL", material.getId(), event.action(),
                        snapshotJson, null, "MaterialChanged", e.getMessage());
            }
        });
    }
}
