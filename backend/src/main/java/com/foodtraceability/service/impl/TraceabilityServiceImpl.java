package com.foodtraceability.service.impl;

import com.foodtraceability.dto.*;
import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import com.foodtraceability.service.TraceabilityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TraceabilityServiceImpl implements TraceabilityService {

    private final ProductRepository productRepository;
    private final MaterialPurchaseRepository materialPurchaseRepository;
    private final StorageRepository storageRepository;
    private final InspectionRepository inspectionRepository;
    private final TransportSaleRepository transportSaleRepository;
    private final ComplaintRepository complaintRepository;

    public TraceabilityServiceImpl(ProductRepository productRepository,
                                   MaterialPurchaseRepository materialPurchaseRepository,
                                   StorageRepository storageRepository,
                                   InspectionRepository inspectionRepository,
                                   TransportSaleRepository transportSaleRepository,
                                   ComplaintRepository complaintRepository) {
        this.productRepository = productRepository;
        this.materialPurchaseRepository = materialPurchaseRepository;
        this.storageRepository = storageRepository;
        this.inspectionRepository = inspectionRepository;
        this.transportSaleRepository = transportSaleRepository;
        this.complaintRepository = complaintRepository;
    }

    @Override
    public Optional<TraceInfoDTO> verifyAndGetTraceInfo(String antiFakeCode) {
        return productRepository.findByAntiFakeCodeAndIsDeletedFalse(antiFakeCode)
                .map(this::buildTraceInfoDTO);
    }

    @Override
    public Optional<TraceInfoDTO> verifyAndGetTraceInfoWithBatch(String antiFakeCode, String batchNumber) {
        return productRepository.findByAntiFakeCodeAndIsDeletedFalse(antiFakeCode)
                .filter(product -> batchNumber.equals(product.getBatchNumber()))
                // Use the already matched Product record to avoid mixing up other batches with the same product name.
                .map(this::buildTraceInfoDTOWithBatch);
    }

    @Override
    public Optional<PurchaseInfoDTO> getPurchaseInfo(String antiFakeCode) {
        return productRepository.findByAntiFakeCodeAndIsDeletedFalse(antiFakeCode)
                .map(this::buildPurchaseInfoDTO);
    }

    @Override
    public List<PurchaseInfoDTO> listAllProducts() {
        return productRepository.findAll().stream()
                .filter(p -> !p.getIsDeleted())
                .map(this::buildPurchaseInfoDTO)
                .collect(Collectors.toList());
    }

    private TraceInfoDTO buildTraceInfoDTOWithBatch(Product product) {
        TraceInfoDTO dto = new TraceInfoDTO();

        TraceInfoDTO.ProductInfo productInfo = new TraceInfoDTO.ProductInfo();
        productInfo.setId(product.getId());
        productInfo.setAntiFakeCode(product.getAntiFakeCode());
        productInfo.setName(product.getName());
        productInfo.setSpecification(product.getSpecification());
        productInfo.setBatchNumber(product.getBatchNumber());
        productInfo.setProductionDate(product.getProductionDate());
        productInfo.setShelfLife(product.getShelfLife());
        productInfo.setLastQueriedTime(product.getLastQueriedTime());
        dto.setProduct(productInfo);

        String antiFakeCode = product.getAntiFakeCode();
        String batchNumber = product.getBatchNumber();

        dto.setMaterialPurchases(materialPurchaseRepository
                .findByAntiFakeCodeAndBatchNumber(antiFakeCode, batchNumber)
                .stream()
                .map(this::toMaterialPurchaseDTO).collect(Collectors.toList()));
        dto.setStorages(storageRepository
                .findByAntiFakeCodeAndBatchNumber(antiFakeCode, batchNumber)
                .stream()
                .map(this::toStorageDTO).collect(Collectors.toList()));
        dto.setInspections(inspectionRepository
                .findByAntiFakeCodeAndBatchNumber(antiFakeCode, batchNumber)
                .stream()
                .map(this::toInspectionDTO).collect(Collectors.toList()));
        dto.setTransportSales(transportSaleRepository
                .findByAntiFakeCodeAndBatchNumber(antiFakeCode, batchNumber)
                .stream()
                .map(this::toTransportSaleDTO).collect(Collectors.toList()));
        dto.setComplaints(List.of());

        return dto;
    }

    private TraceInfoDTO buildTraceInfoDTO(Product product) {
        TraceInfoDTO dto = new TraceInfoDTO();

        TraceInfoDTO.ProductInfo productInfo = new TraceInfoDTO.ProductInfo();
        productInfo.setId(product.getId());
        productInfo.setAntiFakeCode(product.getAntiFakeCode());
        productInfo.setName(product.getName());
        productInfo.setSpecification(product.getSpecification());
        productInfo.setBatchNumber(product.getBatchNumber());
        productInfo.setProductionDate(product.getProductionDate());
        productInfo.setShelfLife(product.getShelfLife());
        productInfo.setLastQueriedTime(product.getLastQueriedTime());
        dto.setProduct(productInfo);

        String antiFakeCode = product.getAntiFakeCode();
        String batchNumber = product.getBatchNumber();

        dto.setMaterialPurchases(materialPurchaseRepository.findByAntiFakeCodeAndBatchNumber(antiFakeCode, batchNumber).stream()
                .map(this::toMaterialPurchaseDTO).collect(Collectors.toList()));
        dto.setStorages(storageRepository.findByAntiFakeCodeAndBatchNumber(antiFakeCode, batchNumber).stream()
                .map(this::toStorageDTO).collect(Collectors.toList()));
        dto.setInspections(inspectionRepository.findByAntiFakeCodeAndBatchNumber(antiFakeCode, batchNumber).stream()
                .map(this::toInspectionDTO).collect(Collectors.toList()));
        dto.setTransportSales(transportSaleRepository.findByAntiFakeCodeAndBatchNumber(antiFakeCode, batchNumber).stream()
                .map(this::toTransportSaleDTO).collect(Collectors.toList()));
        dto.setComplaints(List.of());

        return dto;
    }

    private PurchaseInfoDTO buildPurchaseInfoDTO(Product product) {
        PurchaseInfoDTO dto = new PurchaseInfoDTO();
        dto.setProductId(product.getId());
        dto.setName(product.getName());
        dto.setSpecification(product.getSpecification());
        dto.setBatchNumber(product.getBatchNumber());
        dto.setAntiFakeCode(product.getAntiFakeCode());
        dto.setQrCodeUrl(product.getQrCodeUrl());
        dto.setImageUrl(product.getImageUrl());
        dto.setContactPhone(product.getContactPhone());
        dto.setContactEmail(product.getContactEmail());
        dto.setLastQueriedTime(product.getLastQueriedTime());
        return dto;
    }

    private TraceInfoDTO.MaterialPurchaseDTO toMaterialPurchaseDTO(MaterialPurchase mp) {
        TraceInfoDTO.MaterialPurchaseDTO dto = new TraceInfoDTO.MaterialPurchaseDTO();
        dto.setAntiFakeCode(mp.getAntiFakeCode());
        dto.setBatchNumber(mp.getBatchNumber());
        dto.setMaterialName(mp.getMaterialName());
        dto.setProducerName(mp.getProducerName());
        dto.setProducerAddress(mp.getProducerAddress());
        return dto;
    }

    private TraceInfoDTO.StorageDTO toStorageDTO(Storage s) {
        TraceInfoDTO.StorageDTO dto = new TraceInfoDTO.StorageDTO();
        dto.setAntiFakeCode(s.getAntiFakeCode());
        dto.setBatchNumber(s.getBatchNumber());
        dto.setStorageTime(s.getStorageTime());
        dto.setOutboundTime(s.getOutboundTime());
        dto.setQuantity(s.getQuantity());
        dto.setUnit(s.getUnit());
        return dto;
    }

    private TraceInfoDTO.InspectionDTO toInspectionDTO(Inspection i) {
        TraceInfoDTO.InspectionDTO dto = new TraceInfoDTO.InspectionDTO();
        dto.setAntiFakeCode(i.getAntiFakeCode());
        dto.setBatchNumber(i.getBatchNumber());
        dto.setSampleName(i.getSampleName());
        dto.setSampleQuantity(i.getSampleQuantity());
        dto.setSampleSpecification(i.getSampleSpecification());
        return dto;
    }

    private TraceInfoDTO.TransportSaleDTO toTransportSaleDTO(TransportSale ts) {
        TraceInfoDTO.TransportSaleDTO dto = new TraceInfoDTO.TransportSaleDTO();
        dto.setAntiFakeCode(ts.getAntiFakeCode());
        dto.setBatchNumber(ts.getBatchNumber());
        dto.setEnvironmentTemperature(ts.getEnvironmentTemperature());
        dto.setProductTemperature(ts.getProductTemperature());
        dto.setTime(ts.getTime());
        return dto;
    }

    private TraceInfoDTO.ComplaintDTO toComplaintDTO(Complaint c) {
        TraceInfoDTO.ComplaintDTO dto = new TraceInfoDTO.ComplaintDTO();
        dto.setAntiFakeCode(c.getAntiFakeCode());
        dto.setComplaintReason(c.getComplaintReason());
        dto.setComplaintTime(c.getComplaintTime());
        return dto;
    }
}