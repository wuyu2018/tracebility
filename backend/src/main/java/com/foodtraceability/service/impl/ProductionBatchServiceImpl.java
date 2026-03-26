package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ProductionBatchDTO;
import com.foodtraceability.dto.InspectionDTO;
import com.foodtraceability.dto.StorageDTO;
import com.foodtraceability.dto.TransportSaleDTO;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.ProductionBatchService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductionBatchServiceImpl implements ProductionBatchService {
    @Autowired
    private ProductionBatchRepository batchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MaterialPurchaseRepository materialRepository;

    @Autowired
    private BatchMaterialRelationRepository relationRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private TransportSaleRepository transportSaleRepository;

    private static final AtomicLong batchCounter = new AtomicLong(0);

    @Override
    @Transactional
    public ProductionBatch createBatch(ProductionBatchDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        String batchNumber = generateBatchNumber();

        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber(batchNumber);
        batch.setProduct(product);
        batch.setProductionDate(dto.getProductionDate());
        batch.setShelfLife(dto.getShelfLife() != null ? dto.getShelfLife() : product.getShelfLife());
        batch.setQuantity(dto.getQuantity());
        batch.setUnit(dto.getUnit());
        batch.setIsDeleted(false);
        batch = batchRepository.save(batch);

        if (dto.getMaterialIds() != null && !dto.getMaterialIds().isEmpty()) {
            for (Long materialId : dto.getMaterialIds()) {
                MaterialPurchase material = materialRepository.findById(materialId)
                        .orElseThrow(() -> new RuntimeException("原材料不存在: " + materialId));
                BatchMaterialRelation relation = new BatchMaterialRelation();
                relation.setBatch(batch);
                relation.setMaterial(material);
                relationRepository.save(relation);
            }
        }

        return batch;
    }

    @Override
    @Transactional
    public ProductionBatch updateBatch(Long id, ProductionBatchDTO dto) {
        ProductionBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));

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

        return batchRepository.save(batch);
    }

    @Override
    @Transactional
    public void deleteBatch(Long id) {
        ProductionBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));
        batch.setIsDeleted(true);
        batchRepository.save(batch);
    }

    @Override
    public List<ProductionBatch> listAllBatches() {
        return batchRepository.findByIsDeletedFalse();
    }

    @Override
    public List<ProductionBatch> getBatchesByProductId(Long productId) {
        return batchRepository.findByProductIdAndIsDeletedFalse(productId);
    }

    @Override
    public ProductionBatch getBatchById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生产批次不存在"));
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
        storage.setBatch(batch);
        storage = storageRepository.save(storage);

        batch.setStorageId(storage.getId());
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
        transportSale.setBatch(batch);
        transportSale = transportSaleRepository.save(transportSale);

        batch.setTransportSaleId(transportSale.getId());
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

        String batchNumber = generateBatchNumber();

        ProductionBatch batch = new ProductionBatch();
        batch.setBatchNumber(batchNumber);
        batch.setProduct(product);
        batch.setProductionDate(LocalDate.now().toString());
        batch.setShelfLife(product.getShelfLife());
        batch.setQuantity(0);
        batch.setUnit("");
        batch.setIsDeleted(false);
        return batchRepository.save(batch);
    }
}