package com.foodtraceability.controller;

import com.foodtraceability.dto.InsertDataDTO.*;
import com.foodtraceability.entity.*;
import com.foodtraceability.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insert")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InsertDataController {

    @Autowired
    private ProductService productService;

    @Autowired
    private MaterialPurchaseService materialPurchaseService;

    @Autowired
    private InspectionService inspectionService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private TransportSaleService transportSaleService;

    // 插入产品
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDTO dto) {
        Product created = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 插入原材料采购
    @PostMapping("/material-purchase")
    public ResponseEntity<MaterialPurchase> createMaterialPurchase(@Valid @RequestBody MaterialPurchaseDTO dto) {
        MaterialPurchase created = materialPurchaseService.createMaterialPurchase(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 插入检验检测
    @PostMapping("/inspection")
    public ResponseEntity<Inspection> createInspection(@Valid @RequestBody InspectionDTO dto) {
        Inspection created = inspectionService.createInspection(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 插入仓储
    @PostMapping("/storage")
    public ResponseEntity<Storage> createStorage(@Valid @RequestBody StorageDTO dto) {
        Storage created = storageService.createStorage(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 插入运输销售
    @PostMapping("/transport-sale")
    public ResponseEntity<TransportSale> createTransportSale(@Valid @RequestBody TransportSaleDTO dto) {
        TransportSale created = transportSaleService.createTransportSale(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}