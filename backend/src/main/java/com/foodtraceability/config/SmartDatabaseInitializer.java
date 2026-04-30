package com.foodtraceability.config;

import com.foodtraceability.entity.*;
import com.foodtraceability.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class SmartDatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SmartDatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProductMaterialRelationRepository pmrRepository;

    @Autowired
    private MaterialPurchaseRepository materialPurchaseRepository;

    @Autowired
    private ProductionBatchRepository batchRepository;

    @Autowired
    private BatchMaterialRelationRepository bmrRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private TransportSaleRepository transportSaleRepository;

    @Autowired
    private SecurityCodeRepository securityCodeRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[数据库初始化] 开始检测数据库状态...");

        if (hasExistingData()) {
            log.info("[数据库初始化] 检测到数据库已有数据，跳过初始化");
            return;
        }

        log.info("[数据库初始化] 数据库为空，开始初始化数据...");

        try {
            initializeData();
            log.info("[数据库初始化] 数据库初始化完成");
        } catch (Exception e) {
            log.error("[数据库初始化] 初始化失败: {}", e.getMessage(), e);
        }
    }

    private boolean hasExistingData() {
        try {
            Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product",
                Long.class
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.debug("[数据库初始化] 检测数据库状态时出错: {}", e.getMessage());
            return false;
        }
    }

    private void initializeData() {
        // ===== 1. 产品 =====
        Product milk = createProduct("有机纯牛奶", "250ml/盒", "6个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com");
        Product oil = createProduct("有机橄榄油", "500ml/瓶", "18个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com");
        Product honey = createProduct("有机蜂蜜", "350g/瓶", "24个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com");
        productRepository.saveAll(List.of(milk, oil, honey));
        log.info("[数据库初始化] 产品数据初始化完成");

        // ===== 2. 原料品种 (Material) =====
        Material rawMilk = createMaterial("有机生牛乳");
        Material vitD3 = createMaterial("维生素D3");
        Material oliveFruit = createMaterial("有机橄榄果");
        Material honeyRaw = createMaterial("有机蜂蜜原料");
        materialRepository.saveAll(List.of(rawMilk, vitD3, oliveFruit, honeyRaw));
        log.info("[数据库初始化] 原料品种初始化完成");

        // ===== 3. 产品-原料可见性 (ProductMaterialRelation) =====
        pmrRepository.saveAll(List.of(
            ProductMaterialRelation.create(milk, rawMilk),
            ProductMaterialRelation.create(milk, vitD3),
            ProductMaterialRelation.create(oil, oliveFruit),
            ProductMaterialRelation.create(honey, honeyRaw)
        ));
        log.info("[数据库初始化] 产品-原料可见性初始化完成");

        // ===== 4. 原料采购批次 (MaterialPurchase) =====
        MaterialPurchase mp1 = createMaterialPurchase(rawMilk, "BATCH-M-20240101", "绿源有机牧场", "内蒙古呼和浩特市和林格尔县", "2024-01-05", 10000.0, "升");
        MaterialPurchase mp2 = createMaterialPurchase(vitD3, "BATCH-M-20240102", "华药生物科技", "河北省石家庄市", "2024-01-08", 500.0, "千克");
        MaterialPurchase mp3 = createMaterialPurchase(oliveFruit, "BATCH-M-20240201", "地中海橄榄庄园", "西班牙安达卢西亚", "2024-02-01", 20000.0, "千克");
        MaterialPurchase mp4 = createMaterialPurchase(honeyRaw, "BATCH-M-20240301", "秦岭深山养蜂基地", "陕西省汉中市", "2024-03-05", 5000.0, "千克");
        materialPurchaseRepository.saveAll(List.of(mp1, mp2, mp3, mp4));
        log.info("[数据库初始化] 原料采购批次初始化完成");

        // ===== 5. 生产批次 (ProductionBatch) =====
        ProductionBatch batch1 = createBatch(milk, "B202604300001", LocalDate.of(2024, 1, 15), "6个月", 10000.0, "盒");
        ProductionBatch batch2 = createBatch(oil, "B202604300002", LocalDate.of(2024, 2, 1), "18个月", 5000.0, "瓶");
        ProductionBatch batch3 = createBatch(honey, "B202604300003", LocalDate.of(2024, 3, 10), "24个月", 3000.0, "瓶");
        batchRepository.saveAll(List.of(batch1, batch2, batch3));
        log.info("[数据库初始化] 生产批次初始化完成");

        // ===== 6. 批次-原料关联 (BatchMaterialRelation) =====
        bmrRepository.saveAll(List.of(
            BatchMaterialRelation.create(batch1, mp1),
            BatchMaterialRelation.create(batch1, mp2),
            BatchMaterialRelation.create(batch2, mp3),
            BatchMaterialRelation.create(batch3, mp4)
        ));
        log.info("[数据库初始化] 批次-原料关联初始化完成");

        // ===== 7. 仓储 (Storage) =====
        Storage s1 = createStorage(batch1, "2024-01-16T08:00:00", "2024-01-18T14:00:00", 10000.0, "盒", "A区-01库位");
        Storage s2 = createStorage(batch2, "2024-02-05T10:00:00", "2024-02-07T09:00:00", 5000.0, "瓶", "B区-03库位");
        Storage s3 = createStorage(batch3, "2024-03-12T08:30:00", "2024-03-14T16:00:00", 3000.0, "瓶", "C区-02库位");
        storageRepository.saveAll(List.of(s1, s2, s3));
        log.info("[数据库初始化] 仓储数据初始化完成");

        // ===== 8. 出厂检验 (Inspection) =====
        inspectionRepository.saveAll(List.of(
            Inspection.create(batch1, "有机纯牛奶", 50, "250ml/盒"),
            Inspection.create(batch2, "有机橄榄油", 30, "500ml/瓶"),
            Inspection.create(batch3, "有机蜂蜜", 20, "350g/瓶")
        ));
        log.info("[数据库初始化] 出厂检验数据初始化完成");

        // ===== 9. 储运销售 (TransportSale) =====
        TransportSale t1 = createTransportSale(batch1, 4.0, 2.5, "2024-01-20T10:00:00", "顺丰冷链", "京A·88888", "华北区");
        TransportSale t2 = createTransportSale(batch2, 18.0, 15.0, "2024-02-10T14:00:00", "中通冷链", "沪B·66666", "华东区");
        TransportSale t3 = createTransportSale(batch3, 22.0, 20.0, "2024-03-15T11:00:00", "韵达冷链", "粤C·33333", "华南区");
        transportSaleRepository.saveAll(List.of(t1, t2, t3));
        log.info("[数据库初始化] 储运销售数据初始化完成");

        // ===== 10. 防伪码 (SecurityCode) =====
        SecurityCode sc1 = SecurityCode.create(batch1);
        SecurityCode sc2 = SecurityCode.create(batch2);
        SecurityCode sc3 = SecurityCode.create(batch3);
        securityCodeRepository.saveAll(List.of(sc1, sc2, sc3));
        log.info("[数据库初始化] 防伪码初始化完成");

        // ===== 11. 管理员 =====
        if (adminRepository.findByUsername("admin").isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
            log.info("[数据库初始化] 管理员账号初始化完成 (admin/admin123)");
        } else {
            log.info("[数据库初始化] 管理员账号已存在，跳过初始化");
        }
    }

    private Product createProduct(String name, String specification, String shelfLife, String imageUrl, String phone, String email) {
        Product p = new Product();
        p.setName(name);
        p.setSpecification(specification);
        p.setShelfLife(shelfLife);
        p.setImageUrl(imageUrl);
        p.setContactPhone(phone);
        p.setContactEmail(email);
        p.setIsDeleted(false);
        return p;
    }

    private Material createMaterial(String name) {
        Material m = new Material();
        m.setName(name);
        m.setIsActive(true);
        return m;
    }

    private MaterialPurchase createMaterialPurchase(Material material, String batchNumber, String supplierName, String producerAddress, String purchaseDate, Double quantity, String unit) {
        MaterialPurchase mp = new MaterialPurchase();
        mp.setMaterial(material);
        mp.setBatchNumber(batchNumber);
        mp.setSupplierName(supplierName);
        mp.setProducerAddress(producerAddress);
        mp.setPurchaseDate(LocalDateTime.parse(purchaseDate + "T00:00:00"));
        mp.setQuantity(quantity);
        mp.setUnit(unit);
        mp.setIsDeleted(false);
        return mp;
    }

    private ProductionBatch createBatch(Product product, String batchNumber, LocalDate productionDate, String shelfLife, Double quantity, String unit) {
        ProductionBatch b = new ProductionBatch();
        b.setBatchNumber(batchNumber);
        b.setProduct(product);
        b.setProductionDate(productionDate);
        b.setShelfLife(shelfLife);
        b.setQuantity(quantity);
        b.setUnit(unit);
        b.setIsDeleted(false);
        return b;
    }

    private Storage createStorage(ProductionBatch batch, String storageTime, String outboundTime, Double quantity, String unit, String warehouseLocation) {
        Storage s = new Storage();
        s.associateBatch(batch);
        s.setStorageTime(LocalDateTime.parse(storageTime));
        s.setOutboundTime(LocalDateTime.parse(outboundTime));
        s.setQuantity(quantity);
        s.setUnit(unit);
        s.setWarehouseLocation(warehouseLocation);
        return s;
    }

    private TransportSale createTransportSale(ProductionBatch batch, Double envTemp, Double prodTemp, String time, String company, String vehicle, String region) {
        TransportSale t = new TransportSale();
        t.associateBatch(batch);
        t.setEnvironmentTemperature(envTemp);
        t.setProductTemperature(prodTemp);
        t.setTime(LocalDateTime.parse(time));
        t.setTransportCompany(company);
        t.setVehicleNumber(vehicle);
        t.setSalesRegion(region);
        t.setReceiverName("测试收货人");
        t.setReceiverContact("13800138000");
        return t;
    }
}
