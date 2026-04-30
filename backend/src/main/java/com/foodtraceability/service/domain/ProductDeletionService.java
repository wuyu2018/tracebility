package com.foodtraceability.service.domain;

import com.foodtraceability.domain.DeletionResult;
import com.foodtraceability.entity.Product;
import com.foodtraceability.policy.DeletionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductDeletionService {

    private static final Logger log = LoggerFactory.getLogger(ProductDeletionService.class);

    private final DeletionPolicy deletionPolicy;

    public ProductDeletionService(DeletionPolicy deletionPolicy) {
        this.deletionPolicy = deletionPolicy;
    }

    @Transactional
    public DeletionResult deleteProduct(Product product) {
        deletionPolicy.deleteProduct(product);
        log.info("[产品删除] 删除完成 - ID: {}", product.getId());
        return DeletionResult.create(product.getId(), new Long[0]);
    }
}
