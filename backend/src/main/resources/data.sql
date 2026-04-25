-- =====================================================
-- 食品溯源系统数据库初始化脚本
-- 适配 DDD 重构后的数据模型
-- =====================================================

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除旧表（按依赖顺序）
DROP TABLE IF EXISTS complaint;
DROP TABLE IF EXISTS transport_sale;
DROP TABLE IF EXISTS storage;
DROP TABLE IF EXISTS inspection;
DROP TABLE IF EXISTS material_purchase;
DROP TABLE IF EXISTS batch_material_relation;
DROP TABLE IF EXISTS security_code;
DROP TABLE IF EXISTS production_batch;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS admin;

-- =====================================================
-- 创建 product 表（产品主表）
-- =====================================================
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    name VARCHAR(100) NOT NULL COMMENT '产品名称',
    specification VARCHAR(50) COMMENT '规格',
    shelf_life VARCHAR(50) COMMENT '保质期',
    image_url VARCHAR(500) COMMENT '产品图片路径',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    anti_fake_code VARCHAR(100) COMMENT '防伪码（SC开头，关联security_code表）',
    qr_code_url VARCHAR(500) COMMENT '二维码URL',
    UNIQUE KEY uk_anti_fake_code (anti_fake_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品信息表';

-- =====================================================
-- 创建 production_batch 表（生产批次）
-- =====================================================
CREATE TABLE production_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    batch_number VARCHAR(50) NOT NULL COMMENT '批次号',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    production_date DATE NOT NULL COMMENT '生产日期',
    shelf_life VARCHAR(50) COMMENT '保质期',
    quantity DOUBLE COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    storage_id BIGINT COMMENT '仓储信息ID',
    transport_sale_id BIGINT COMMENT '运输销售ID',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_product_batch (product_id, batch_number),
    CONSTRAINT fk_batch_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产批次表';

-- =====================================================
-- 创建 security_code 表（防伪码）
-- =====================================================
CREATE TABLE security_code (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    code VARCHAR(64) NOT NULL COMMENT '防伪码（SC开头）',
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    status VARCHAR(20) NOT NULL DEFAULT '未激活' COMMENT '状态：未激活/已激活',
    first_scan_time TIMESTAMP NULL COMMENT '首次扫码时间',
    scan_count INT DEFAULT 0 COMMENT '扫码次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_code (code),
    INDEX idx_batch_id (batch_id),
    CONSTRAINT fk_security_code_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='防伪码表';

-- =====================================================
-- 创建 material_purchase 表（原材料采购）
-- =====================================================
CREATE TABLE material_purchase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    material_name VARCHAR(100) NOT NULL COMMENT '原料名称',
    batch_number VARCHAR(50) NOT NULL COMMENT '采购批次号',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    producer_name VARCHAR(100) COMMENT '生产商名称',
    producer_address VARCHAR(255) COMMENT '生产商地址',
    purchase_date TIMESTAMP NULL COMMENT '采购日期',
    quantity DOUBLE COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否删除',
    UNIQUE KEY uk_product_material_batch (product_id, batch_number),
    CONSTRAINT fk_material_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原材料采购记录';

-- =====================================================
-- 创建 storage 表（仓储信息）
-- =====================================================
CREATE TABLE storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    storage_time TIMESTAMP NULL COMMENT '入库时间',
    outbound_time TIMESTAMP NULL COMMENT '出库时间',
    quantity DOUBLE COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位',
    warehouse_location VARCHAR(100) COMMENT '仓库位置',
    CONSTRAINT fk_storage_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓储记录';

-- =====================================================
-- 创建 inspection 表（出厂检验）
-- =====================================================
CREATE TABLE inspection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    sample_name VARCHAR(100) COMMENT '样品名称',
    sample_quantity INT COMMENT '抽样数量',
    sample_specification VARCHAR(100) COMMENT '样品规格',
    image_url VARCHAR(500) COMMENT '检验报告图片URL',
    CONSTRAINT fk_inspection_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出厂检验记录';

-- =====================================================
-- 创建 transport_sale 表（运输销售）
-- =====================================================
CREATE TABLE transport_sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    environment_temperature DECIMAL(5,2) COMMENT '环境温度',
    product_temperature DECIMAL(5,2) COMMENT '产品温度',
    time TIMESTAMP NULL COMMENT '运输时间',
    transport_company VARCHAR(100) COMMENT '运输公司',
    vehicle_number VARCHAR(50) COMMENT '车牌号',
    sales_region VARCHAR(255) COMMENT '销售区域',
    receiver_name VARCHAR(100) COMMENT '收货人姓名',
    receiver_contact VARCHAR(50) COMMENT '收货人联系方式',
    CONSTRAINT fk_transport_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输销售记录';

-- =====================================================
-- 创建 complaint 表（投诉信息）
-- =====================================================
CREATE TABLE complaint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    anti_fake_code VARCHAR(64) COMMENT '防伪码',
    complaint_reason TEXT COMMENT '投诉原因',
    complaint_time TIMESTAMP NULL COMMENT '投诉时间',
    product_name VARCHAR(100) COMMENT '产品名称',
    batch_number VARCHAR(50) COMMENT '批次号',
    is_processed BOOLEAN DEFAULT FALSE COMMENT '是否已处理'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉信息表';

-- =====================================================
-- 创建 admin 表（管理员）
-- =====================================================
CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    email VARCHAR(100) COMMENT '邮箱',
    is_locked BOOLEAN DEFAULT FALSE COMMENT '是否锁定',
    login_attempts INT DEFAULT 0 COMMENT '登录尝试次数',
    last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- =====================================================
-- 创建 batch_material_relation 表（批次-原材料关联）
-- =====================================================
CREATE TABLE batch_material_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    material_id BIGINT NOT NULL COMMENT '原材料ID',
    CONSTRAINT fk_relation_batch FOREIGN KEY (batch_id) REFERENCES production_batch(id) ON DELETE CASCADE,
    CONSTRAINT fk_relation_material FOREIGN KEY (material_id) REFERENCES material_purchase(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次原材料关联表';

-- =====================================================
-- 更新 production_batch 表，添加 storage_id 和 transport_sale_id 外键
-- =====================================================
ALTER TABLE production_batch
    ADD CONSTRAINT fk_batch_storage FOREIGN KEY (storage_id) REFERENCES storage(id),
    ADD CONSTRAINT fk_batch_transport FOREIGN KEY (transport_sale_id) REFERENCES transport_sale(id);

-- =====================================================
-- 插入测试数据
-- =====================================================

-- 插入管理员 (密码: admin123)
INSERT INTO admin (username, password, email) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye0B4z7dFqHqmT5BZBhqLcOqDXqKQ3G8u', 'admin@foodtrace.com'),
('wuyu2018', '$2a$10$N9qo8uLOickgx2ZMRZoMye0B4z7dFqHqmT5BZBhqLcOqDXqKQ3G8u', 'wuyu2018@foodtrace.com');

-- 插入产品
INSERT INTO product (name, specification, shelf_life, image_url, contact_phone, contact_email) VALUES
('有机纯牛奶', '250ml/盒', '6个月', '/img/products/milk.svg', '18788919351', '2896114330@qq.com'),
('有机橄榄油', '500ml/瓶', '18个月', '/img/products/oil.svg', '18788919351', '2896114330@qq.com'),
('有机蜂蜜', '350g/瓶', '24个月', '/img/products/honey.svg', '18788919351', '2896114330@qq.com');

-- 插入生产批次
INSERT INTO production_batch (batch_number, product_id, production_date, shelf_life, quantity, unit) VALUES
('BATCH20240101', 1, '2024-01-15', '6个月', 10000, '盒'),
('BATCH20240201', 2, '2024-02-01', '18个月', 5000, '瓶'),
('BATCH20240301', 3, '2024-03-10', '24个月', 3000, '瓶');

-- 插入防伪码（SC开头）
INSERT INTO security_code (code, batch_id, status, scan_count) VALUES
('SC20240115000001ABCD', 1, '未激活', 0),
('SC20240115000002EFGH', 1, '未激活', 0),
('SC20240201000001IJKL', 2, '未激活', 0),
('SC20240201000002MNOP', 2, '未激活', 0),
('SC20240310000001QRST', 3, '未激活', 0),
('SC20240310000002UVWX', 3, '未激活', 0);

-- 更新产品的防伪码字段（使用第一个防伪码）
UPDATE product SET anti_fake_code = 'SC20240115000001ABCD' WHERE id = 1;
UPDATE product SET anti_fake_code = 'SC20240201000001IJKL' WHERE id = 2;
UPDATE product SET anti_fake_code = 'SC20240310000001QRST' WHERE id = 3;

-- 插入原材料采购记录
INSERT INTO material_purchase (product_id, material_name, batch_number, supplier_name, producer_name, producer_address, purchase_date, quantity, unit) VALUES
(1, '有机生牛乳', 'BATCH20240101', '绿源供应链', '绿源有机牧场', '内蒙古呼和浩特市和林格尔县', '2024-01-10', 5000, '升'),
(1, '维生素D3', 'BATCH20240101', '医药采购', '华药生物科技', '河北省石家庄市', '2024-01-11', 100, '克'),
(2, '有机橄榄果', 'BATCH20240201', '进口贸易', '地中海橄榄庄园', '西班牙安达卢西亚', '2024-01-20', 2000, '公斤'),
(3, '有机蜂蜜原料', 'BATCH20240301', '蜂农合作社', '秦岭深山养蜂基地', '陕西省汉中市', '2024-02-15', 500, '公斤');

-- 插入批次-原材料关联
INSERT INTO batch_material_relation (batch_id, material_id) VALUES
(1, 1), (1, 2),
(2, 3),
(3, 4);

-- 插入仓储信息
INSERT INTO storage (batch_id, storage_time, outbound_time, quantity, unit, warehouse_location) VALUES
(1, '2024-01-16 08:00:00', '2024-01-18 14:00:00', 10000, '盒', 'A区-01号冷库'),
(2, '2024-02-05 10:00:00', '2024-02-07 09:00:00', 5000, '瓶', 'B区-02号仓库'),
(3, '2024-03-12 08:30:00', '2024-03-14 16:00:00', 3000, '瓶', 'C区-03号仓库');

-- 更新批次的仓储ID
UPDATE production_batch SET storage_id = 1 WHERE id = 1;
UPDATE production_batch SET storage_id = 2 WHERE id = 2;
UPDATE production_batch SET storage_id = 3 WHERE id = 3;

-- 插入检验记录
INSERT INTO inspection (batch_id, sample_name, sample_quantity, sample_specification, image_url) VALUES
(1, '有机纯牛奶', 50, '250ml/盒', '/img/inspection/milk-report.svg'),
(2, '有机橄榄油', 30, '500ml/瓶', '/img/inspection/oil-report.svg'),
(3, '有机蜂蜜', 20, '350g/瓶', '/img/inspection/honey-report.svg');

-- 插入运输销售记录
INSERT INTO transport_sale (batch_id, environment_temperature, product_temperature, time, transport_company, vehicle_number, sales_region, receiver_name, receiver_contact) VALUES
(1, 4.0, 2.5, '2024-01-20 10:00:00', '冷链物流公司', '京A12345', '北京市朝阳区', '张经理', '13800138001'),
(2, 18.0, 15.0, '2024-02-10 14:00:00', '安达物流', '沪B67890', '上海市浦东新区', '李经理', '13800138002'),
(3, 22.0, 20.0, '2024-03-15 11:00:00', '顺丰速运', '粤C11111', '广州市天河区', '王经理', '13800138003');

-- 更新批次的运输销售ID
UPDATE production_batch SET transport_sale_id = 1 WHERE id = 1;
UPDATE production_batch SET transport_sale_id = 2 WHERE id = 2;
UPDATE production_batch SET transport_sale_id = 3 WHERE id = 3;

-- 插入投诉记录
INSERT INTO complaint (anti_fake_code, complaint_reason, complaint_time, product_name, batch_number, is_processed) VALUES
('SC20240115000001ABCD', '包装轻微破损', '2024-02-01 09:30:00', '有机纯牛奶', 'BATCH20240101', FALSE),
('SC20240201000001IJKL', '产品有异味', '2024-02-15 14:20:00', '有机橄榄油', 'BATCH20240201', FALSE);

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 验证数据
SELECT '=== 产品表 ===' AS '';
SELECT id, name, anti_fake_code FROM product;
SELECT '=== 批次表 ===' AS '';
SELECT pb.id, pb.batch_number, p.name, pb.storage_id, pb.transport_sale_id
FROM production_batch pb
JOIN product p ON pb.product_id = p.id;
SELECT '=== 防伪码表 ===' AS '';
SELECT sc.id, sc.code, sc.batch_id, sc.status FROM security_code sc;
SELECT '=== 仓储表 ===' AS '';
SELECT * FROM storage;
SELECT '=== 运输销售表 ===' AS '';
SELECT * FROM transport_sale;
