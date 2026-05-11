package com.foodtraceability.traceability.application.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.repository.MaterialRepository;
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
    private final ObjectMapper objectMapper;

    public MaterialChangedEventListener(MaterialRepository repository, BlockchainService blockchainService) {
        this.repository = repository;
        this.blockchainService = blockchainService;
        this.objectMapper = new ObjectMapper();
    }

    @TransactionalEventListener
    public void onMaterialChanged(MaterialChanged event) {
        log.info("[Event] MaterialChanged: id={}, action={}", event.materialId(), event.action());

        repository.findById(event.materialId()).ifPresent(material -> {
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", material.getId());
                snapshot.put("name", material.getName());
                snapshot.put("isActive", material.isActive());

                if ("DEACTIVATE".equals(event.action())) {
                    snapshot.put("is_deleted", true);
                }

                blockchainService.appendMaterialChainBlock(
                        "MATERIAL", material.getId(), event.action(),
                        objectMapper.writeValueAsString(snapshot), null);
                log.info("[Blockchain] Material block appended: id={}, action={}",
                        material.getId(), event.action());
            } catch (Exception e) {
                log.error("[Blockchain] Failed to append block for Material {}", event.action(), e);
            }
        });
    }
}
