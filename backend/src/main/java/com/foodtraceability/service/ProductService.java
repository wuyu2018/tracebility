package com.foodtraceability.service;

import com.foodtraceability.dto.InsertDataDTO.*;
import com.foodtraceability.entity.*;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductDTO dto);
    Product generateQrCode(Long productId);
    List<Product> batchGenerateQrCodes(List<Long> productIds);
    void batchDeleteProducts(List<Long> productIds);
    List<Product> listAllProducts();
    List<Product> searchProducts(String keyword);
}
