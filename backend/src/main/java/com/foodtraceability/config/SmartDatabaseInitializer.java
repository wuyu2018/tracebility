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
        products.add(createProduct("有机纯牛奶", "250ml/盒", "6个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com"));
        products.add(createProduct("有机橄榄油", "500ml/瓶", "18个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com"));
        products.add(createProduct("有机蜂蜜", "350g/瓶", "24个月", "/img/products/placeholder.svg", "18788919351", "2896114330@qq.com"));
        productRepository.saveAll(products);
        log.info("[数据库初始化] 产品数据初始化完成");

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
}