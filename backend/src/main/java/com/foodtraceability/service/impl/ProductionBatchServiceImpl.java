package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ProductionBatchDTO;
import com.foodtraceability.dto.InspectionDTO;
import com.foodtraceability.dto.StorageDTO;
import com.foodtraceability.dto.TransportSaleDTO;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.ProductionBatchService;
import com.foodtraceability.validator.BatchMaterialValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductionBatchServiceImpl implements ProductionBatchService {
    private final ProductionBatchRepository batchRepository;
    private final ProductRepository productRepository;
    private final MaterialPurchaseRepository materialRepository;
    private final BatchMaterialRelationRepository relationRepository;
    private final InspectionRepository inspectionRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;

    private static final AtomicLong batchCounter = new AtomicLong(0);

    private final BatchMaterialValidator batchMaterialValidator;

    @Autowired
    public ProductionBatchServiceImpl(ProductionBatchRepository batchRepository,
                                     ProductRepository productRepository,
                                     MaterialPurchaseRepository materialRepository,
                                     BatchMaterialRelationRepository relationRepository,
                                     InspectionRepository inspectionRepository,
                                     StorageRepository storageRepository,
                                     TransportSaleRepository transportSaleRepository,
                                     BatchMaterialValidator batchMaterialValidator) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.materialRepository = materialRepository;
        this.relationRepository = relationRepository;
        this.inspectionRepository = inspectionRepository;
        this.storageRepository = storageRepository;
        this.transportSaleRepository = transportSaleRepository;
        this.batchMaterialValidator = batchMaterialValidator;
    }

    @PostConstruct
    public void initBatchCounter() {
        batchRepository.findAll().stream()
                .map(ProductionBatch::getBatchNumber)
                .filter(n -> n != null && n.matches("B\\d{8}\\d{4}"))
                .map(n -> n.substring(9))
                .mapToLong(Long::parseLong)
                .max()
                .ifPresent(max -> batchCounter.set(max));
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

        return batch;
    }

    private ProductionBatch createBatchEntity(ProductionBatchDTO dto, Product product) {
        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber(generateBatchNumber());
        batch.setProduct(product);
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
            batchMaterialValidator.validate(batch.getId(), mpId);
            MaterialPurchase materialPurchase = materialRepository.findById(mpId)
                    .orElseThrow(() -> new RuntimeException("原材料采购记录不存在: " + mpId));
            BatchMaterialRelation relation = BatchMaterialRelation.create(batch, materialPurchase);
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
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("产品不存在"));
            batch.setProduct(product);
        }
        if (dto.getProductionDate() != null) {
            batch.setProductionDate(dto.getProductionDate());
        }
        if (dto.getShelfLife() != null) {
            batch.setShelfLife(dto.getShelfLife());
        }
        if (dto.getQuantity() != null) {
            batch.setQuantity(dto.getQuantity());
        }
        if (dto.getUnit() != null) {
            batch.setUnit(dto.getUnit());
        }
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
        inspection.setBatch(batch);
        inspection = inspectionRepository.save(inspection);

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

        TransportSaleDTO result = new TransportSaleDTO();
        BeanUtils.copyProperties(transportSale, result);
        return result;
    }

    private String generateBatchNumber() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = batchCounter.incrementAndGet();
        return "B" + dateStr + String.format("%04d", counter);
    }

    @Override
    @Transactional
    public ProductionBatch createQuickBatchForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber(generateBatchNumber());
        batch.setProduct(product);
        batch.setProductionDate(LocalDate.now());
        batch.setShelfLife(product.getShelfLife());
        batch.setQuantity(0.0);
        batch.setUnit("");
        batch.setIsDeleted(false);
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