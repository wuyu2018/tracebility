package com.foodtraceability.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtraceability.dto.ProductionBatchDTO;
import com.foodtraceability.dto.InspectionDTO;
import com.foodtraceability.dto.StorageDTO;
import com.foodtraceability.dto.TransportSaleDTO;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.BlockchainService;
import com.foodtraceability.service.ProductionBatchService;
import com.foodtraceability.validator.BatchMaterialValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductionBatchServiceImpl implements ProductionBatchService {
    private static final Logger log = LoggerFactory.getLogger(ProductionBatchServiceImpl.class);

    private final ProductionBatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final MaterialPurchaseRepository materialRepository;
    private final BatchMaterialRelationRepository relationRepository;
    private final InspectionRepository inspectionRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;
    private final BlockchainService blockchainService;
    private final ObjectMapper objectMapper;

    private final BatchMaterialValidator batchMaterialValidator;

    @Autowired
    public ProductionBatchServiceImpl(ProductionBatchRepository batchRepository,
                                      ProductRepository productRepository,
                                      MaterialPurchaseRepository materialRepository,
                                      BatchMaterialRelationRepository relationRepository,
                                      InspectionRepository inspectionRepository,
                                      StorageRepository storageRepository,
                                      TransportSaleRepository transportSaleRepository,
                                      BlockchainService blockchainService,
                                      BatchMaterialValidator batchMaterialValidator) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.materialRepository = materialRepository;
        this.relationRepository = relationRepository;
        this.inspectionRepository = inspectionRepository;
        this.storageRepository = storageRepository;
        this.transportSaleRepository = transportSaleRepository;
        this.blockchainService = blockchainService;
        this.objectMapper = new ObjectMapper();
        this.batchMaterialValidator = batchMaterialValidator;
    }

    private long nextBatchSeq() {
        return batchRepository.findByIsDeletedFalse().stream()
                .map(ProductionBatch::getBatchNumber)
                .filter(n -> n != null && n.matches("B\\d{8}\\d{4}"))
                .map(n -> n.substring(9))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0) + 1;
    }

    @Override
    @Transactional
    public ProductionBatch createBatch(ProductionBatchDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        ProductionBatch batch = createBatchEntity(dto, product);
        batch = batchRepository.save(batch);

        associateMaterials(batch, dto.getMaterialIds());
        associateStorage(batch, dto.getStorage());
        associateTransportSale(batch, dto.getTransportSale());

        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("id", batch.getId());
            snapshot.put("batchNumber", batch.getBatchNumber());
            snapshot.put("productId", batch.getProductId());
            snapshot.put("productionDate", batch.getProductionDate() != null ? batch.getProductionDate().toString() : null);
            snapshot.put("shelfLife", batch.getShelfLife());
            snapshot.put("quantity", batch.getQuantity());
            snapshot.put("unit", batch.getUnit());
            blockchainService.appendBatchChainBlock(batch.getId(), "PRODUCTION_BATCH", batch.getId(), "CREATE",
                    objectMapper.writeValueAsString(snapshot), null);
        } catch (Exception e) {
            log.error("[Blockchain] Failed to append block for ProductionBatch CREATE (V1)", e);
        }

        return batch;
    }

    private ProductionBatch createBatchEntity(ProductionBatchDTO dto, Product product) {
        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber(generateBatchNumber());
        batch.setProductId(product.getId());
        batch.setProductionDate(dto.getProductionDate());
        batch.setShelfLife(dto.getShelfLife() != null ? dto.getShelfLife() : product.getShelfLife());
        batch.setQuantity(dto.getQuantity());
        batch.setUnit(dto.getUnit());
        batch.setIsDeleted(false);
        return batch;
    }

    private void associateMaterials(ProductionBatch batch, List<Long> materialPurchaseIds) {
        if (materialPurchaseIds == null || materialPurchaseIds.isEmpty()) {
            return;
        }
        for (Long mpId : materialPurchaseIds) {
            MaterialPurchase materialPurchase = materialRepository.findById(mpId)
                    .orElseThrow(() -> new RuntimeException("原材料采购记录不存在: " + mpId));
            BatchMaterialRelation relation = BatchMaterialRelation.create(batch.getId(), mpId);
            relationRepository.save(relation);
        }
    }

    private void associateStorage(ProductionBatch batch, StorageDTO storageDto) {
        if (storageDto == null) {
            return;
        }
        Storage storage = new Storage();
        BeanUtils.copyProperties(storageDto, storage);
        storage.associateBatch(batch);
        storage = storageRepository.save(storage);
        batch.associateStorage(storage);
        batchRepository.save(batch);
    }

    private void associateTransportSale(ProductionBatch batch, TransportSaleDTO transportSaleDto) {
        if (transportSaleDto == null) {
            return;
        }
        TransportSale transportSale = new TransportSale();
        BeanUtils.copyProperties(transportSaleDto, transportSale);
        transportSale.associateBatch(batch);
        transportSale = transportSaleRepository.save(transportSale);
        batch.associateTransportSale(transportSale);
        batchRepository.save(batch);
    }

    @Override
    @Transactional
    public ProductionBatch updateBatch(Long id, ProductionBatchDTO dto) {
        ProductionBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

        updateBatchFields(batch, dto);
        updateStorage(batch, dto.getStorage());
        updateTransportSale(batch, dto.getTransportSale());

        return batchRepository.save(batch);
    }

    private void updateBatchFields(ProductionBatch batch, ProductionBatchDTO dto) {
        if (dto.getProductId() != null) {
            productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("产品不存在"));
            batch.setProductId(dto.getProductId());
        }
        batch.changeProductionDate(dto.getProductionDate());
        batch.changeShelfLife(dto.getShelfLife());
        batch.changeQuantity(dto.getQuantity());
        batch.changeUnit(dto.getUnit());
    }

    private void updateStorage(ProductionBatch batch, StorageDTO storageDto) {
        if (storageDto == null) {
            return;
        }
        Storage storage = batch.getStorageId() != null
                ? storageRepository.findById(batch.getStorageId()).orElse(new Storage())
                : new Storage();
        BeanUtils.copyProperties(storageDto, storage);
        storage.associateBatch(batch);
        storageRepository.save(storage);
        if (batch.getStorageId() == null) {
            batch.associateStorage(storage);
            batchRepository.save(batch);
        }
    }

    private void updateTransportSale(ProductionBatch batch, TransportSaleDTO transportSaleDto) {
        if (transportSaleDto == null) {
            return;
        }
        TransportSale transportSale = batch.getTransportSaleId() != null
                ? transportSaleRepository.findById(batch.getTransportSaleId()).orElse(new TransportSale())
                : new TransportSale();
        BeanUtils.copyProperties(transportSaleDto, transportSale);
        transportSale.associateBatch(batch);
        transportSaleRepository.save(transportSale);
        if (batch.getTransportSaleId() == null) {
            batch.associateTransportSale(transportSale);
            batchRepository.save(batch);
        }
    }

    @Override
    @Transactional
    public void deleteBatch(Long id) {
        ProductionBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));
        batch.softDelete();
        batchRepository.save(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionBatchDTO> listAllBatches() {
        return batchRepository.findByIsDeletedFalse().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionBatchDTO> getBatchesByProductId(Long productId) {
        return batchRepository.findByProductIdAndIsDeletedFalse(productId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionBatchDTO getBatchById(Long id) {
        ProductionBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));
        return toDTO(batch);
    }

    @Override
    public ProductionBatch getBatchByBatchNumber(String batchNumber) {
        return batchRepository.findByBatchNumberAndIsDeletedFalse(batchNumber)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));
    }

    @Override
    @Transactional
    public InspectionDTO addInspection(Long batchId, InspectionDTO dto) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

        Inspection inspection = new Inspection();
        BeanUtils.copyProperties(dto, inspection);
        inspection.setBatchId(batch.getId());
        inspection = inspectionRepository.save(inspection);

        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("id", inspection.getId());
            snapshot.put("batchId", inspection.getBatchId());
            snapshot.put("sampleName", inspection.getSampleName());
            snapshot.put("sampleQuantity", inspection.getSampleQuantity());
            snapshot.put("sampleSpecification", inspection.getSampleSpecification());
            snapshot.put("resultStatus", inspection.getResultStatus());
            snapshot.put("resultDetail", inspection.getResultDetail());
            blockchainService.appendBatchChainBlock(batchId, "INSPECTION", inspection.getId(), "CREATE",
                    objectMapper.writeValueAsString(snapshot), null);
        } catch (Exception e) {
            log.error("[Blockchain] Failed to append block for Inspection CREATE (V1)", e);
        }

        InspectionDTO result = new InspectionDTO();
        BeanUtils.copyProperties(inspection, result);
        return result;
    }

    @Override
    @Transactional
    public StorageDTO addStorage(Long batchId, StorageDTO dto) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

        Storage storage = new Storage();
        BeanUtils.copyProperties(dto, storage);
        storage.associateBatch(batch);
        storage = storageRepository.save(storage);

        batch.associateStorage(storage);
        batchRepository.save(batch);

        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("id", storage.getId());
            snapshot.put("batchId", storage.getBatchId());
            snapshot.put("storageTime", storage.getStorageTime() != null ? storage.getStorageTime().toString() : null);
            snapshot.put("quantity", storage.getQuantity());
            snapshot.put("unit", storage.getUnit());
            snapshot.put("warehouseLocation", storage.getWarehouseLocation());
            blockchainService.appendBatchChainBlock(batchId, "STORAGE", storage.getId(), "CREATE",
                    objectMapper.writeValueAsString(snapshot), null);
        } catch (Exception e) {
            log.error("[Blockchain] Failed to append block for Storage CREATE (V1)", e);
        }

        StorageDTO result = new StorageDTO();
        BeanUtils.copyProperties(storage, result);
        return result;
    }

    @Override
    @Transactional
    public TransportSaleDTO addTransportSale(Long batchId, TransportSaleDTO dto) {
        ProductionBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

        TransportSale transportSale = new TransportSale();
        BeanUtils.copyProperties(dto, transportSale);
        transportSale.associateBatch(batch);
        transportSale = transportSaleRepository.save(transportSale);

        batch.associateTransportSale(transportSale);
        batchRepository.save(batch);

        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("id", transportSale.getId());
            snapshot.put("batchId", transportSale.getBatchId());
            snapshot.put("environmentTemperature", transportSale.getEnvironmentTemperature());
            snapshot.put("productTemperature", transportSale.getProductTemperature());
            snapshot.put("time", transportSale.getTime() != null ? transportSale.getTime().toString() : null);
            snapshot.put("transportCompany", transportSale.getTransportCompany());
            snapshot.put("vehicleNumber", transportSale.getVehicleNumber());
            snapshot.put("salesRegion", transportSale.getSalesRegion());
            snapshot.put("receiverName", transportSale.getReceiverName());
            snapshot.put("receiverContact", transportSale.getReceiverContact());
            blockchainService.appendBatchChainBlock(batchId, "TRANSPORT_SALE", transportSale.getId(), "CREATE",
                    objectMapper.writeValueAsString(snapshot), null);
        } catch (Exception e) {
            log.error("[Blockchain] Failed to append block for TransportSale CREATE (V1)", e);
        }

        TransportSaleDTO result = new TransportSaleDTO();
        BeanUtils.copyProperties(transportSale, result);
        return result;
    }

    private String generateBatchNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = nextBatchSeq();
        return "B" + dateStr + String.format("%04d", counter);
    }

    @Override
    @Transactional
    public ProductionBatch createQuickBatchForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        ProductionBatch batch = ProductionBatch.quickCreate(
                generateBatchNumber(), product.getId(), product.getShelfLife());
        return batchRepository.save(batch);
    }

    private ProductionBatchDTO toDTO(ProductionBatch batch) {
        ProductionBatchDTO dto = new ProductionBatchDTO();
        dto.setId(batch.getId());
        dto.setBatchNumber(batch.getBatchNumber());
        if (batch.getProduct() != null) {
            dto.setProductId(batch.getProduct().getId());
            dto.setProductName(batch.getProduct().getName());
        }
        dto.setProductionDate(batch.getProductionDate());
        dto.setShelfLife(batch.getShelfLife());
        dto.setQuantity(batch.getQuantity());
        dto.setUnit(batch.getUnit());
        dto.setCreatedAt(batch.getCreatedAt());
        return dto;
    }
}