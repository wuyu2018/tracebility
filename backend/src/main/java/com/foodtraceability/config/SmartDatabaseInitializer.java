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
    private MaterialPurchaseRepository materialPurchaseRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private TransportSaleRepository transportSaleRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

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
        List<Product> products = new ArrayList<>();
        products.add(createProduct("AF202400012345", "有机纯牛奶", "250ml/盒", "BATCH20240101", 
            LocalDate.of(2024, 1, 15), "6个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com"));
        products.add(createProduct("AF202400067890", "有机橄榄油", "500ml/瓶", "BATCH20240201", 
            LocalDate.of(2024, 2, 1), "18个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com"));
        products.add(createProduct("AF202400099999", "有机蜂蜜", "350g/瓶", "BATCH20240301", 
            LocalDate.of(2024, 3, 10), "24个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com"));
        productRepository.saveAll(products);
        log.info("[数据库初始化] 产品数据初始化完成");

        List<MaterialPurchase> materialPurchases = new ArrayList<>();
        materialPurchases.add(createMaterialPurchase("有机纯牛奶", "BATCH20240101", "有机生牛乳", "绿源有机牧场", "内蒙古呼和浩特市和林格尔县"));
        materialPurchases.add(createMaterialPurchase("有机纯牛奶", "BATCH20240101", "维生素D3", "华药生物科技", "河北省石家庄市"));
        materialPurchases.add(createMaterialPurchase("有机橄榄油", "BATCH20240201", "有机橄榄果", "地中海橄榄庄园", "西班牙安达卢西亚"));
        materialPurchases.add(createMaterialPurchase("有机蜂蜜", "BATCH20240301", "有机蜂蜜原料", "秦岭深山养蜂基地", "陕西省汉中市"));
        materialPurchaseRepository.saveAll(materialPurchases);
        log.info("[数据库初始化] 原料采购数据初始化完成");

        List<Storage> storages = new ArrayList<>();
        storages.add(createStorage("有机纯牛奶", "BATCH20240101", LocalDateTime.of(2024, 1, 16, 8, 0), LocalDateTime.of(2024, 1, 18, 14, 0), 10000.0, "盒"));
        storages.add(createStorage("有机橄榄油", "BATCH20240201", LocalDateTime.of(2024, 2, 5, 10, 0), LocalDateTime.of(2024, 2, 7, 9, 0), 5000.0, "瓶"));
        storages.add(createStorage("有机蜂蜜", "BATCH20240301", LocalDateTime.of(2024, 3, 12, 8, 30), LocalDateTime.of(2024, 3, 14, 16, 0), 3000.0, "瓶"));
        storageRepository.saveAll(storages);
        log.info("[数据库初始化] 贮存数据初始化完成");

        List<Inspection> inspections = new ArrayList<>();
        inspections.add(createInspection("有机纯牛奶", "BATCH20240101", "有机纯牛奶", 50, "250ml/盒"));
        inspections.add(createInspection("有机橄榄油", "BATCH20240201", "有机橄榄油", 30, "500ml/瓶"));
        inspections.add(createInspection("有机蜂蜜", "BATCH20240301", "有机蜂蜜", 20, "350g/瓶"));
        inspectionRepository.saveAll(inspections);
        log.info("[数据库初始化] 出厂检验数据初始化完成");

        List<TransportSale> transportSales = new ArrayList<>();
        transportSales.add(createTransportSale("有机纯牛奶", "BATCH20240101", 4.0, 2.5, LocalDateTime.of(2024, 1, 20, 10, 0)));
        transportSales.add(createTransportSale("有机橄榄油", "BATCH20240201", 18.0, 15.0, LocalDateTime.of(2024, 2, 10, 14, 0)));
        transportSales.add(createTransportSale("有机蜂蜜", "BATCH20240301", 22.0, 20.0, LocalDateTime.of(2024, 3, 15, 11, 0)));
        transportSaleRepository.saveAll(transportSales);
        log.info("[数据库初始化] 储运销售数据初始化完成");

        complaintRepository.save(createComplaint("有机纯牛奶", "包装轻微破损", LocalDateTime.of(2024, 2, 1, 9, 30)));
        log.info("[数据库初始化] 投诉数据初始化完成");

        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        adminRepository.save(admin);
        log.info("[数据库初始化] 管理员账号初始化完成 (admin/admin123)");
    }

    private Product createProduct(String antiFakeCode, String name, String specification, String batchNumber,
                                  String shelfLife, String imageUrl, String phone, String email) {
        Product p = new Product();
        p.setAntiFakeCode(antiFakeCode);
        p.setName(name);
        p.setSpecification(specification);
        p.setBatchNumber(batchNumber);
        p.setShelfLife(shelfLife);
        p.setImageUrl(imageUrl);
        p.setContactPhone(phone);
        p.setContactEmail(email);
        return p;
    }

    private MaterialPurchase createMaterialPurchase(String productName, String batchNumber, String materialName,
                                                   String producerName, String producerAddress) {
        MaterialPurchase mp = new MaterialPurchase();
        mp.setProductName(productName);
        mp.setBatchNumber(batchNumber);
        mp.setMaterialName(materialName);
        mp.setProducerName(producerName);
        mp.setProducerAddress(producerAddress);
        return mp;
    }

    private Storage createStorage(String productName, String batchNumber, LocalDateTime storageTime,
                                  LocalDateTime outboundTime, Double quantity, String unit) {
        Storage s = new Storage();
        s.setProductName(productName);
        s.setBatchNumber(batchNumber);
        s.setStorageTime(storageTime);
        s.setOutboundTime(outboundTime);
        s.setQuantity(quantity);
        s.setUnit(unit);
        return s;
    }

    private Inspection createInspection(String productName, String batchNumber, String sampleName,
                                       Integer sampleQuantity, String sampleSpecification) {
        Inspection i = new Inspection();
        i.setProductName(productName);
        i.setBatchNumber(batchNumber);
        i.setSampleName(sampleName);
        i.setSampleQuantity(sampleQuantity);
        i.setSampleSpecification(sampleSpecification);
        return i;
    }

    private TransportSale createTransportSale(String productName, String batchNumber,
                                              Double envTemp, Double productTemp, LocalDateTime time) {
        TransportSale ts = new TransportSale();
        ts.setProductName(productName);
        ts.setBatchNumber(batchNumber);
        ts.setEnvironmentTemperature(envTemp);
        ts.setProductTemperature(productTemp);
        ts.setTime(time);
        return ts;
    }

    private Complaint createComplaint(String productName, String reason, LocalDateTime complaintTime) {
        Complaint c = new Complaint();
        c.setProductName(productName);
        c.setComplaintReason(reason);
        c.setComplaintTime(complaintTime);
        return c;
    }
}
