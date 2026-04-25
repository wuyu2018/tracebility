package com.foodtraceability.service.impl;

import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository repository;
    private final ProductionBatchRepository batchRepository;
    private final InspectionRepository inspectionRepository;
    private final StorageRepository storageRepository;
    private final TransportSaleRepository transportSaleRepository;
    private final BatchMaterialRelationRepository relationRepository;
    private final SecurityCodeRepository securityCodeRepository;
    private final MaterialPurchaseRepository materialPurchaseRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository repository,
                             ProductionBatchRepository batchRepository,
                             InspectionRepository inspectionRepository,
                             StorageRepository storageRepository,
                             TransportSaleRepository transportSaleRepository,
                             BatchMaterialRelationRepository relationRepository,
                             SecurityCodeRepository securityCodeRepository,
                             MaterialPurchaseRepository materialPurchaseRepository) {
        this.repository = repository;
        this.batchRepository = batchRepository;
        this.inspectionRepository = inspectionRepository;
        this.storageRepository = storageRepository;
        this.transportSaleRepository = transportSaleRepository;
        this.relationRepository = relationRepository;
        this.securityCodeRepository = securityCodeRepository;
        this.materialPurchaseRepository = materialPurchaseRepository;
    }

    @Override
    @Transactional
    public Product createProduct(ProductDTO dto) {
        Product entity = new Product();
        BeanUtils.copyProperties(dto, entity);
        entity.setIsDeleted(false);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDTO dto) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("[产品删除] 开始删除产品 ID: {}", id);

        List<ProductionBatch> batches = findBatchesByProductId(id);
        deleteRelatedMaterialPurchases(id);
        deleteRelatedBatchMaterialRelations(batches);
        deleteRelatedSecurityCodes(id);
        deleteRelatedInspections(id);
        deleteRelatedStorages(id);
        deleteRelatedTransportSales(id);
        batchRepository.deleteAll(batches);
        repository.deleteById(id);

        log.info("[产品删除] 删除完成");
    }

    private List<ProductionBatch> findBatchesByProductId(Long productId) {
        return batchRepository.findAll().stream()
                .filter(b -> b.getProduct() != null && b.getProduct().getId().equals(productId))
                .toList();
    }

    private void deleteRelatedMaterialPurchases(Long productId) {
        List<MaterialPurchase> materials = materialPurchaseRepository.findAll().stream()
                .filter(m -> m.getProduct() != null && m.getProduct().getId().equals(productId))
                .toList();
        materialPurchaseRepository.deleteAll(materials);
    }

    private void deleteRelatedBatchMaterialRelations(List<ProductionBatch> batches) {
        for (ProductionBatch batch : batches) {
            List<BatchMaterialRelation> relations = relationRepository.findByBatchId(batch.getId());
            relationRepository.deleteAll(relations);
        }
    }

    private void deleteRelatedSecurityCodes(Long productId) {
        List<SecurityCode> codes = securityCodeRepository.findAll().stream()
                .filter(sc -> sc.getBatch() != null && sc.getBatch().getProduct() != null
                        && sc.getBatch().getProduct().getId().equals(productId))
                .toList();
        securityCodeRepository.deleteAll(codes);
    }

    private void deleteRelatedInspections(Long productId) {
        List<Inspection> inspections = inspectionRepository.findAll().stream()
                .filter(i -> i.getBatch() != null && i.getBatch().getProduct() != null
                        && i.getBatch().getProduct().getId().equals(productId))
                .toList();
        inspectionRepository.deleteAll(inspections);
    }

    private void deleteRelatedStorages(Long productId) {
        List<Storage> storages = storageRepository.findAll().stream()
                .filter(s -> s.getBatch() != null && s.getBatch().getProduct() != null
                        && s.getBatch().getProduct().getId().equals(productId))
                .toList();
        storageRepository.deleteAll(storages);
    }

    private void deleteRelatedTransportSales(Long productId) {
        List<TransportSale> transportSales = transportSaleRepository.findAll().stream()
                .filter(t -> t.getBatch() != null && t.getBatch().getProduct() != null
                        && t.getBatch().getProduct().getId().equals(productId))
                .toList();
        transportSaleRepository.deleteAll(transportSales);
    }

    @Override
    @Transactional
    public void clearQrCode(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
        entity.clearQrCode();
        repository.save(entity);
    }

    @Override
    public List<Product> listAllProducts() {
        return repository.findByIsDeletedFalse();
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAllProducts();
        }
        return repository.findByNameContainingAndIsDeletedFalse(keyword.trim());
    }

    @Override
    public Product getProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("产品不存在"));
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<Product> getProductByAntiFakeCode(String antiFakeCode) {
        return repository.findByAntiFakeCode(antiFakeCode);
    }
}