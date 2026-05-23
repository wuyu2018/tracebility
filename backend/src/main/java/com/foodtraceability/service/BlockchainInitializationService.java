package com.foodtraceability.service;

import com.foodtraceability.agent.service.AgentBlockchainService;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BlockchainInitializationService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainInitializationService.class);

    private final BlockchainLogRepository blockchainLogRepo;
    private final AgentBlockchainService agentBlockchainService;
    private final MaterialRepository materialRepo;
    private final MaterialPurchaseRepository materialPurchaseRepo;
    private final ProductionBatchRepository batchRepo;
    private final StorageRepository storageRepo;
    private final InspectionRepository inspectionRepo;
    private final TransportSaleRepository transportSaleRepo;

    public BlockchainInitializationService(BlockchainLogRepository blockchainLogRepo,
                                            AgentBlockchainService agentBlockchainService,
                                            MaterialRepository materialRepo,
                                            MaterialPurchaseRepository materialPurchaseRepo,
                                            ProductionBatchRepository batchRepo,
                                            StorageRepository storageRepo,
                                            InspectionRepository inspectionRepo,
                                            TransportSaleRepository transportSaleRepo) {
        this.blockchainLogRepo = blockchainLogRepo;
        this.agentBlockchainService = agentBlockchainService;
        this.materialRepo = materialRepo;
        this.materialPurchaseRepo = materialPurchaseRepo;
        this.batchRepo = batchRepo;
        this.storageRepo = storageRepo;
        this.inspectionRepo = inspectionRepo;
        this.transportSaleRepo = transportSaleRepo;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void initializeBlockchain() {
        if (blockchainLogRepo.existsByChainType("MATERIAL") || blockchainLogRepo.existsByChainType("BATCH")) {
            log.info("[BlockchainInit] Blockchain data already exists, skipping initialization");
            return;
        }

        log.info("[BlockchainInit] Starting blockchain initialization for historical data...");

        initMaterialChain();
        initBatchChains();

        log.info("[BlockchainInit] Blockchain initialization completed");
    }

    private void initMaterialChain() {
        List<Material> materials = materialRepo.findAll();
        for (Material m : materials) {
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", m.getId());
                snapshot.put("name", m.getName());
                snapshot.put("isActive", m.isActive());
                agentBlockchainService.appendBlockWithConsensus(
                        "MATERIAL", "MATERIAL", m.getId(), "CREATE",
                        toJson(snapshot), null);
            } catch (Exception e) {
                log.warn("[BlockchainInit] Failed to init block for material id={}", m.getId(), e);
            }
        }

        List<MaterialPurchase> purchases = materialPurchaseRepo.findByIsDeletedFalse();
        for (MaterialPurchase mp : purchases) {
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", mp.getId());
                snapshot.put("materialId", mp.getMaterial().getId());
                snapshot.put("materialName", mp.getMaterialName());
                snapshot.put("batchNumber", mp.getBatchNumber());
                snapshot.put("supplierName", mp.getSupplierName());
                snapshot.put("quantity", mp.getQuantity());
                snapshot.put("unit", mp.getUnit());
                agentBlockchainService.appendBlockWithConsensus(
                        "MATERIAL", "MATERIAL_PURCHASE", mp.getId(), "CREATE",
                        toJson(snapshot), null);
            } catch (Exception e) {
                log.warn("[BlockchainInit] Failed to init block for materialPurchase id={}", mp.getId(), e);
            }
        }

        log.info("[BlockchainInit] Material chain initialized: {} materials, {} purchases",
                materials.size(), purchases.size());
    }

    private void initBatchChains() {
        List<ProductionBatch> batches = batchRepo.findByIsDeletedFalse();
        for (ProductionBatch batch : batches) {
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", batch.getId());
                snapshot.put("batchNumber", batch.getBatchNumber());
                snapshot.put("productId", batch.getProductId());
                snapshot.put("productionDate", batch.getProductionDate() != null ? batch.getProductionDate().toString() : null);
                snapshot.put("shelfLife", batch.getShelfLife());
                snapshot.put("quantity", batch.getQuantity());
                snapshot.put("unit", batch.getUnit());
                snapshot.put("storageId", batch.getStorageId());
                snapshot.put("transportSaleId", batch.getTransportSaleId());
                snapshot.put("isDeleted", batch.isDeleted());
                snapshot.put("createdAt", batch.getCreatedAt() != null ? batch.getCreatedAt().toString() : null);
                agentBlockchainService.appendBlockWithConsensus(
                        "BATCH", "PRODUCTION_BATCH", batch.getId(), "CREATE",
                        toJson(snapshot), null);
            } catch (Exception e) {
                log.warn("[BlockchainInit] Failed to init genesis block for batch id={}", batch.getId(), e);
            }

            if (batch.getStorageId() != null) {
                storageRepo.findById(batch.getStorageId()).ifPresent(s -> {
                    try {
                        Map<String, Object> snapshot = new LinkedHashMap<>();
                        snapshot.put("id", s.getId());
                        snapshot.put("batchId", s.getBatchId());
                        snapshot.put("storageTime", s.getStorageTime() != null ? s.getStorageTime().toString() : null);
                        snapshot.put("quantity", s.getQuantity());
                        snapshot.put("unit", s.getUnit());
                        snapshot.put("warehouseLocation", s.getWarehouseLocation());
                        agentBlockchainService.appendBlockWithConsensus(
                                "BATCH", "STORAGE", s.getId(), "CREATE",
                                toJson(snapshot), null);
                    } catch (Exception e2) {
                        log.warn("[BlockchainInit] Failed to init storage block for storage id={}", s.getId(), e2);
                    }
                });
            }
        }

        List<Inspection> inspections = inspectionRepo.findAll();
        for (Inspection insp : inspections) {
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", insp.getId());
                snapshot.put("batchId", insp.getBatchId());
                snapshot.put("sampleName", insp.getSampleName());
                snapshot.put("sampleQuantity", insp.getSampleQuantity());
                snapshot.put("sampleSpecification", insp.getSampleSpecification());
                snapshot.put("resultStatus", insp.getResultStatus());
                snapshot.put("resultDetail", insp.getResultDetail());
                snapshot.put("inspectorName", insp.getInspectorName());
                snapshot.put("inspectionTime", insp.getInspectionTime() != null ? insp.getInspectionTime().toString() : null);
                agentBlockchainService.appendBlockWithConsensus(
                        "BATCH", "INSPECTION", insp.getId(), "CREATE",
                        toJson(snapshot), null);
            } catch (Exception e) {
                log.warn("[BlockchainInit] Failed to init inspection block for inspection id={}", insp.getId(), e);
            }
        }

        List<TransportSale> transportSales = transportSaleRepo.findAll();
        for (TransportSale ts : transportSales) {
            try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("id", ts.getId());
                snapshot.put("batchId", ts.getBatchId());
                snapshot.put("transportCompany", ts.getTransportCompany());
                snapshot.put("vehicleNumber", ts.getVehicleNumber());
                snapshot.put("salesRegion", ts.getSalesRegion());
                snapshot.put("receiverName", ts.getReceiverName());
                snapshot.put("receiverContact", ts.getReceiverContact());
                snapshot.put("recorderName", ts.getRecorderName());
                snapshot.put("time", ts.getTime() != null ? ts.getTime().toString() : null);
                agentBlockchainService.appendBlockWithConsensus(
                        "BATCH", "TRANSPORT_SALE", ts.getId(), "CREATE",
                        toJson(snapshot), null);
            } catch (Exception e) {
                log.warn("[BlockchainInit] Failed to init transportSale block for transportSale id={}", ts.getId(), e);
            }
        }

        log.info("[BlockchainInit] Batch chains initialized: {} batches", batches.size());
    }

    private String toJson(Map<String, Object> map) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            return map.toString();
        }
    }
}
