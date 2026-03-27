package com.foodtraceability.service;

import com.foodtraceability.dto.ProductDTO;
import com.foodtraceability.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product createProduct(ProductDTO dto);
    Product updateProduct(Long id, ProductDTO dto);
    void deleteProduct(Long id);
    List<Product> listAllProducts();
    List<Product> searchProducts(String keyword);
    Product getProductById(Long id);
    Optional<Product> getProductByAntiFakeCode(String antiFakeCode);
}