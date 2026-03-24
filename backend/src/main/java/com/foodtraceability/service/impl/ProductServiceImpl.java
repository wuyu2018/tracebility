package com.foodtraceability.service.impl;

import com.foodtraceability.dto.InsertDataDTO.ProductDTO;
import com.foodtraceability.entity.Product;
import com.foodtraceability.repository.ProductRepository;
import com.foodtraceability.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository repository;

    @Value("${app.verify-url:http://localhost:5173}")
    private String verifyUrl;

    @Override
    @Transactional
    public Product createProduct(ProductDTO dto) {
        if (repository.existsByAntiFakeCode(dto.getAntiFakeCode())) {
            throw new RuntimeException("防伪码已存在，请勿重复录入！");
        }
        Product entity = new Product();
        entity.setAntiFakeCode(dto.getAntiFakeCode());
        entity.setName(dto.getName());
        entity.setSpecification(dto.getSpecification());
        entity.setBatchNumber(dto.getBatchNumber());
        entity.setShelfLife(dto.getShelfLife());
        entity.setImageUrl(dto.getImageUrl());
        entity.setContactPhone(dto.getContactPhone());
        entity.setContactEmail(dto.getContactEmail());
        entity.setIsDeleted(false);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public Product generateQrCode(Long productId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("产品不存在"));

        String qrContent = verifyUrl + "?code=" + product.getAntiFakeCode();
        String qrCodeData = generateQrCodeAsDataUrl(qrContent);
        product.setQrCodeUrl(qrCodeData);
        return repository.save(product);
    }

    @Override
    @Transactional
    public List<Product> batchGenerateQrCodes(List<Long> productIds) {
        return productIds.stream().map(id -> {
            try {
                return generateQrCode(id);
            } catch (Exception e) {
                throw new RuntimeException("批量生成失败，产品ID: " + id + ", 错误: " + e.getMessage());
            }
        }).toList();
    }

    @Override
    @Transactional
    public void batchDeleteProducts(List<Long> productIds) {
        for (Long id : productIds) {
            repository.findById(id).ifPresent(product -> {
                product.setQrCodeUrl(null);
                repository.save(product);
            });
        }
    }

    @Override
    public List<Product> listAllProducts() {
        return repository.findAll().stream()
                .filter(p -> !p.getIsDeleted())
                .toList();
    }

    private String generateQrCodeAsDataUrl(String content) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrBytes = outputStream.toByteArray();
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            throw new RuntimeException("二维码生成失败: " + e.getMessage());
        }
    }
}