package com.foodtraceability.service;

import com.foodtraceability.dto.ProductionBatchDTO;
import com.foodtraceability.dto.InspectionDTO;
import com.foodtraceability.dto.StorageDTO;
import com.foodtraceability.dto.TransportSaleDTO;
import com.foodtraceability.entity.ProductionBatch;

import java.util.List;

public interface ProductionBatchService {
    ProductionBatch createBatch(ProductionBatchDTO dto);
    ProductionBatch updateBatch(Long id, ProductionBatchDTO dto);
    void deleteBatch(Long id);
    List<ProductionBatch> listAllBatches();
    List<ProductionBatch> getBatchesByProductId(Long productId);
    ProductionBatch getBatchById(Long id);
    ProductionBatch getBatchByBatchNumber(String batchNumber);
    
    InspectionDTO addInspection(Long batchId, InspectionDTO dto);
    StorageDTO addStorage(Long batchId, StorageDTO dto);
    TransportSaleDTO addTransportSale(Long batchId, TransportSaleDTO dto);
    
    ProductionBatch createQuickBatchForProduct(Long productId);
}