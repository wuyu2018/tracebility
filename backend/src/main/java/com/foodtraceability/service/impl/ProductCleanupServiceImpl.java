package com.foodtraceability.service.impl;

import com.foodtraceability.entity.Product;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.ProductCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Service
public class ProductCleanupServiceImpl implements ProductCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ProductCleanupServiceImpl.class);
    private static final int RETENTION_DAYS = 7;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private MaterialPurchaseRepository materialPurchaseRepository;
    
    @Autowired
    private StorageRepository storageRepository;
    
    @Autowired
    private InspectionRepository inspectionRepository;
    
    @Autowired
    private TransportSaleRepository transportSaleRepository;
    
    @Autowired
    private ComplaintRepository complaintRepository;

    @Override
    @Transactional
    public void updateQueryTime(String antiFakeCode) {
        Product product = productRepository.findByAntiFakeCodeAndIsDeletedFalse(antiFakeCode).orElse(null);
        if (product != null) {
            product.setLastQueriedTime(LocalDateTime.now());
            productRepository.save(product);
        }
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredProducts() {
        log.info("[产品清理] 开始清理 {} 天前查询过的产品数据", RETENTION_DAYS);

        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(RETENTION_DAYS);
        
        List<Product> expiredProducts = productRepository.findByIsDeletedFalseAndLastQueriedTimeBefore(cutoffTime);
        Set<String> productNamesToMaybeCleanupComplaints = new HashSet<>();
        for (Product p : expiredProducts) {
            if (p.getName() != null) {
                productNamesToMaybeCleanupComplaints.add(p.getName());
            }
        }
        
        int deletedCount = 0;
        for (Product product : expiredProducts) {
            String productName = product.getName();
            String batchNumber = product.getBatchNumber();
            
            log.info("[产品清理] 清理产品 - 防伪码: {}, 产品名: {}, 批次: {}", 
                maskCode(product.getAntiFakeCode()), productName, batchNumber);
            
            materialPurchaseRepository.deleteByProductNameAndBatchNumber(productName, batchNumber);
            storageRepository.deleteByProductNameAndBatchNumber(productName, batchNumber);
            inspectionRepository.deleteByProductNameAndBatchNumber(productName, batchNumber);
            transportSaleRepository.deleteByProductNameAndBatchNumber(productName, batchNumber);
            
            product.setIsDeleted(true);
            productRepository.save(product);
            
            deletedCount++;
        }

        // 投诉数据按 productName 关联（没有批次字段）。因此只有当该产品下已不存在任何未删除批次时，
        // 才删除对应投诉，避免误删其它仍在保留期内的批次投诉记录。
        for (String productName : productNamesToMaybeCleanupComplaints) {
            List<Product> remaining = productRepository.findByNameAndIsDeletedFalse(productName);
            if (remaining.isEmpty()) {
                complaintRepository.deleteByProductName(productName);
            }
        }

        log.info("[产品清理] 共清理 {} 个产品的数据", deletedCount);
    }
    
    private String maskCode(String code) {
        if (code == null || code.length() <= 8) {
            return "***";
        }
        return code.substring(0, 4) + "****" + code.substring(code.length() - 4);
    }
}
