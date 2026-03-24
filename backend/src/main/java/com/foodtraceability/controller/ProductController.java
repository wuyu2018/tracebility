package com.foodtraceability.controller;

import com.foodtraceability.entity.Product;
import com.foodtraceability.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/select")
    public ResponseEntity<?> selectProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "consumer") String role) {
        
        List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchProducts(keyword.trim());
        } else {
            products = productService.listAllProducts();
        }

        if ("admin".equalsIgnoreCase(role)) {
            List<Map<String, Object>> result = products.stream()
                    .map(p -> Map.<String, Object>of(
                            "id", p.getId(),
                            "antiFakeCode", p.getAntiFakeCode(),
                            "name", p.getName() != null ? p.getName() : "",
                            "batchNumber", p.getBatchNumber() != null ? p.getBatchNumber() : "",
                            "productionDate", p.getProductionDate() != null ? p.getProductionDate().toString() : "",
                            "specification", p.getSpecification() != null ? p.getSpecification() : ""
                    ))
                    .toList();
            return ResponseEntity.ok(result);
        } else {
            List<Map<String, Object>> result = products.stream()
                    .map(p -> Map.<String, Object>of(
                            "id", p.getId(),
                            "name", p.getName() != null ? p.getName() : "",
                            "batchNumber", p.getBatchNumber() != null ? p.getBatchNumber() : "",
                            "productionDate", p.getProductionDate() != null ? p.getProductionDate().toString() : "",
                            "specification", p.getSpecification() != null ? p.getSpecification() : ""
                    ))
                    .toList();
            return ResponseEntity.ok(result);
        }
    }
}
