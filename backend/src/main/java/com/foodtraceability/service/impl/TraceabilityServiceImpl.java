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
        return productRepository.findByAntiFakeCode(antiFakeCode)
                .map(this::buildTraceInfoDTO);
    }

    @Override
    public Optional<TraceInfoDTO> verifyAndGetTraceInfoWithBatch(String antiFakeCode, String batchNumber) {
        return productRepository.findByAntiFakeCode(antiFakeCode)
                .filter(product -> batchNumber.equals(product.getBatchNumber()))
                .map(product -> buildTraceInfoDTOWithBatch(product.getName(), batchNumber));
    }

    @Override
    public Optional<PurchaseInfoDTO> getPurchaseInfo(String antiFakeCode) {
        return productRepository.findByAntiFakeCode(antiFakeCode)
                .map(this::buildPurchaseInfoDTO);
    }

    @Override
    public List<PurchaseInfoDTO> listAllProducts() {
        return productRepository.findAll().stream()
                .map(this::buildPurchaseInfoDTO)
                .collect(Collectors.toList());
    }

    private TraceInfoDTO buildTraceInfoDTOWithBatch(String productName, String batchNumber) {
        TraceInfoDTO dto = new TraceInfoDTO();

        Product product = productRepository.findByName(productName).orElse(null);
        if (product != null) {
            TraceInfoDTO.ProductInfo productInfo = new TraceInfoDTO.ProductInfo();
            productInfo.setId(product.getId());
            productInfo.setAntiFakeCode(product.getAntiFakeCode());
            productInfo.setName(product.getName());
            productInfo.setSpecification(product.getSpecification());
            productInfo.setBatchNumber(batchNumber);
            productInfo.setProductionDate(product.getProductionDate());
            productInfo.setShelfLife(product.getShelfLife());
            dto.setProduct(productInfo);
        }

        dto.setMaterialPurchases(materialPurchaseRepository.findByProductNameAndBatchNumber(productName, batchNumber).stream()
                .map(this::toMaterialPurchaseDTO).collect(Collectors.toList()));
        dto.setStorages(storageRepository.findByProductNameAndBatchNumber(productName, batchNumber).stream()
                .map(this::toStorageDTO).collect(Collectors.toList()));
        dto.setInspections(inspectionRepository.findByProductNameAndBatchNumber(productName, batchNumber).stream()
                .map(this::toInspectionDTO).collect(Collectors.toList()));
        dto.setTransportSales(transportSaleRepository.findByProductNameAndBatchNumber(productName, batchNumber).stream()
                .map(this::toTransportSaleDTO).collect(Collectors.toList()));
        dto.setComplaints(complaintRepository.findByProductName(productName).stream()
            .map(this::toComplaintDTO).collect(Collectors.toList()));

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
        dto.setProduct(productInfo);

        String productName = product.getName();
        String batchNumber = product.getBatchNumber();

        dto.setMaterialPurchases(materialPurchaseRepository.findByProductNameAndBatchNumber(productName, batchNumber).stream()
                .map(this::toMaterialPurchaseDTO).collect(Collectors.toList()));
        dto.setStorages(storageRepository.findByProductNameAndBatchNumber(productName, batchNumber).stream()
                .map(this::toStorageDTO).collect(Collectors.toList()));
        dto.setInspections(inspectionRepository.findByProductNameAndBatchNumber(productName, batchNumber).stream()
                .map(this::toInspectionDTO).collect(Collectors.toList()));
        dto.setTransportSales(transportSaleRepository.findByProductNameAndBatchNumber(productName, batchNumber).stream()
                .map(this::toTransportSaleDTO).collect(Collectors.toList()));
        dto.setComplaints(complaintRepository.findByProductName(product.getName()).stream()
            .map(this::toComplaintDTO).collect(Collectors.toList()));

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
        return dto;
    }

    private TraceInfoDTO.MaterialPurchaseDTO toMaterialPurchaseDTO(MaterialPurchase mp) {
        TraceInfoDTO.MaterialPurchaseDTO dto = new TraceInfoDTO.MaterialPurchaseDTO();
        dto.setProductName(mp.getProductName());
        dto.setBatchNumber(mp.getBatchNumber());
        dto.setMaterialName(mp.getMaterialName());
        dto.setProducerName(mp.getProducerName());
        dto.setProducerAddress(mp.getProducerAddress());
        return dto;
    }

    private TraceInfoDTO.StorageDTO toStorageDTO(Storage s) {
        TraceInfoDTO.StorageDTO dto = new TraceInfoDTO.StorageDTO();
        dto.setProductName(s.getProductName());
        dto.setBatchNumber(s.getBatchNumber());
        dto.setStorageTime(s.getStorageTime());
        dto.setOutboundTime(s.getOutboundTime());
        dto.setQuantity(s.getQuantity());
        dto.setUnit(s.getUnit());
        return dto;
    }

    private TraceInfoDTO.InspectionDTO toInspectionDTO(Inspection i) {
        TraceInfoDTO.InspectionDTO dto = new TraceInfoDTO.InspectionDTO();
        dto.setProductName(i.getProductName());
        dto.setBatchNumber(i.getBatchNumber());
        dto.setSampleName(i.getSampleName());
        dto.setSampleQuantity(i.getSampleQuantity());
        dto.setSampleSpecification(i.getSampleSpecification());
        return dto;
    }

    private TraceInfoDTO.TransportSaleDTO toTransportSaleDTO(TransportSale ts) {
        TraceInfoDTO.TransportSaleDTO dto = new TraceInfoDTO.TransportSaleDTO();
        dto.setProductName(ts.getProductName());
        dto.setBatchNumber(ts.getBatchNumber());
        dto.setEnvironmentTemperature(ts.getEnvironmentTemperature());
        dto.setProductTemperature(ts.getProductTemperature());
        dto.setTime(ts.getTime());
        return dto;
    }

    private TraceInfoDTO.ComplaintDTO toComplaintDTO(Complaint c) {
        TraceInfoDTO.ComplaintDTO dto = new TraceInfoDTO.ComplaintDTO();
        dto.setComplaintReason(c.getComplaintReason());
        dto.setComplaintTime(c.getComplaintTime());
        return dto;
    }
}