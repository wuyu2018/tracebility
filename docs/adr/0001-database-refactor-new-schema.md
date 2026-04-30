# ADR-0001: 数据库重构 — 新 Schema 设计

基于 grilling 会话的共识产出 DDL。

## 核心变更

- Material 从 MaterialPurchase 中拆分，成为独立品种表
- ProductMaterialRelation 中间表控制产品可见性
- 删除操作语义分离：软删（is_deleted/is_active） vs 物理删（仅限于无关联记录时）
- BatchMaterialRelation 改为复合主键

## DDL

```sql
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 删除旧表（重构后重建）
-- ============================================================
DROP TABLE IF EXISTS complaint;
DROP TABLE IF EXISTS security_code;
DROP TABLE IF EXISTS batch_material_relation;
DROP TABLE IF EXISTS inspection;
DROP TABLE IF EXISTS transport_sale;
DROP TABLE IF EXISTS storage;
DROP TABLE IF EXISTS material_purchase;
DROP TABLE IF EXISTS product_material_relation;
DROP TABLE IF EXISTS production_batch;
DROP TABLE IF EXISTS material;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS admin;

-- ============================================================
-- 1. Product（产品）
-- ============================================================
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specification VARCHAR(50),
    shelf_life VARCHAR(50),
    image_url VARCHAR(500),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    anti_fake_code VARCHAR(100),
    qr_code_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品';

-- ============================================================
-- 2. Material（原料品种，独立于产品）
-- ============================================================
CREATE TABLE material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_material_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料品种';

-- ============================================================
-- 3. ProductMaterialRelation（产品可见性控制）
-- ============================================================
CREATE TABLE product_material_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE KEY uk_product_material (product_id, material_id),
    CONSTRAINT fk_pmr_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT fk_pmr_material FOREIGN KEY (material_id) REFERENCES material(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品-原料可见性关联';

-- ============================================================
-- 4. MaterialPurchase（原料采购批次）
-- ============================================================
CREATE TABLE material_purchase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_id BIGINT NOT NULL,
    batch_number VARCHAR(50) COMMENT '供应商批次号',
    supplier_name VARCHAR(100),
    producer_name VARCHAR(100),
    producer_address VARCHAR(255),
    purchase_date DATETIME,
    quantity DOUBLE,
    unit VARCHAR(20),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mp_material FOREIGN KEY (material_id) REFERENCES material(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料采购批次';

-- ============================================================
-- 5. ProductionBatch（生产批次）
-- ============================================================
CREATE TABLE production_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_number VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    production_date DATE,
    shelf_life VARCHAR(50),
    quantity DOUBLE,
    unit VARCHAR(20),
    storage_id BIGINT,
    transport_sale_id BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_batch (product_id, batch_number),
    CONSTRAINT fk_pb_product FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产批次';

-- ============================================================
-- 6. BatchMaterialRelation（批次使用原料，复合主键）
-- ============================================================
CREATE TABLE batch_material_relation (
    batch_id BIGINT NOT NULL,
    material_purchase_id BIGINT NOT NULL,
    PRIMARY KEY (batch_id, material_purchase_id),
    CONSTRAINT fk_bmr_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id),
    CONSTRAINT fk_bmr_material_purchase FOREIGN KEY (material_purchase_id) REFERENCES material_purchase(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次-原料采购关联';

-- ============================================================
-- 7. Storage（仓储）
-- ============================================================
CREATE TABLE storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT,
    storage_time DATETIME,
    outbound_time DATETIME,
    quantity DOUBLE,
    unit VARCHAR(20),
    warehouse_location VARCHAR(100),
    CONSTRAINT fk_storage_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓储记录';

-- ============================================================
-- 8. Inspection（出厂检验）
-- ============================================================
CREATE TABLE inspection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    sample_name VARCHAR(100),
    sample_quantity INT,
    sample_specification VARCHAR(100),
    image_url VARCHAR(500),
    CONSTRAINT fk_inspection_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出厂检验';

-- ============================================================
-- 9. TransportSale（储运销售）
-- ============================================================
CREATE TABLE transport_sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT,
    environment_temperature DOUBLE,
    product_temperature DOUBLE,
    time DATETIME,
    transport_company VARCHAR(100),
    vehicle_number VARCHAR(50),
    sales_region VARCHAR(255),
    receiver_name VARCHAR(100),
    receiver_contact VARCHAR(50),
    CONSTRAINT fk_ts_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='储运销售';

-- ============================================================
-- 10. SecurityCode（防伪码）—— 已改为指向 ProductionBatch
-- ============================================================
CREATE TABLE security_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    batch_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT '未激活',
    first_scan_time DATETIME,
    scan_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_security_code (code),
    CONSTRAINT fk_sc_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='防伪码';

-- ============================================================
-- 11. Complaint（投诉）—— 指向 SecurityCode
-- ============================================================
CREATE TABLE complaint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    security_code_id BIGINT,
    complaint_reason TEXT,
    complaint_time DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_security_code FOREIGN KEY (security_code_id) REFERENCES security_code(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉';

-- ============================================================
-- 12. Admin（管理员）
-- ============================================================
CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员';

SET FOREIGN_KEY_CHECKS = 1;
```

## 与旧 schema 的关键差异

| 旧表/字段 | 新表/字段 | 原因 |
|---|---|---|
| material_purchase.product_id | → 删除 | 原料不再属于特定产品 |
| material_purchase.material_name | → material.name | 品种拆分 |
| material_purchase — | → material_purchase.material_id | 新 FK 指向 material |
| — | product_material_relation | 中间表控制可见性 |
| batch_material_relation (自增 id) | 复合主键 (batch_id, material_purchase_id) | 避免自增浪费 |
| complaint.anti_fake_code / product_name / batch_number | complaint.security_code_id | 通过 FK 关联，消除冗余 |
| security_code — | security_code.batch_id | 指向 production_batch（当前已是） |

## 应用层约束（不由 DB 强制）

- 创建 BatchMaterialRelation 前：校验 MaterialPurchase 所属 Material 对 Product 的 ProductMaterialRelation.is_hidden ≠ true
- Product 有关联 Batch/SecurityCode 时：禁止物理 DELETE
- Complaint 创建时：通过 antiFakeCode 查到 SecurityCode.id 再写入
