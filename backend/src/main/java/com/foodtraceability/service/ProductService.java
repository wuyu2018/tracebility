package com.foodtraceability.service;

import com.foodtraceability.dto.InsertDataDTO.*;
import com.foodtraceability.entity.*;

public interface ProductService {
    Product createProduct(ProductDTO dto);
    Product generateQrCode(Long productId);
}
