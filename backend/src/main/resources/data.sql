-- 禁用外键检查，以便安全删除和重建表
SET FOREIGN_KEY_CHECKS = 0;

-- 创建 product 表（产品主表）
CREATE TABLE product (
    id INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    anti_fake_code VARCHAR(50) NOT NULL UNIQUE COMMENT '防伪码',
    name VARCHAR(100) NOT NULL COMMENT '产品名称',
    specification VARCHAR(50) COMMENT '规格',
    batch_number VARCHAR(50) NOT NULL COMMENT '批号',
    production_date DATE COMMENT '生产日期',
    shelf_life VARCHAR(20) COMMENT '保质期（如 6个月）',
    image_url VARCHAR(255) COMMENT '产品图片路径',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品信息表';

-- 创建 material_purchase 表（原料采购）
CREATE TABLE material_purchase (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    batch_number VARCHAR(50) NOT NULL COMMENT '批号',
    material_name VARCHAR(100) NOT NULL COMMENT '原料名称',
    producer_name VARCHAR(100) COMMENT '生产商名称',
    producer_address VARCHAR(200) COMMENT '生产商地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='原料采购记录';

-- 创建 storage 表（贮存记录）
CREATE TABLE storage (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    batch_number VARCHAR(50) NOT NULL COMMENT '批号',
    storage_time DATETIME COMMENT '入库时间',
    outbound_time DATETIME COMMENT '出库时间',
    quantity INT COMMENT '数量',
    unit VARCHAR(20) COMMENT '单位（如盒、瓶）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='贮存记录';

-- 创建 inspection 表（出厂检验）
CREATE TABLE inspection (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    batch_number VARCHAR(50) NOT NULL COMMENT '批号',
    sample_name VARCHAR(100) COMMENT '样品名称',
    sample_quantity INT COMMENT '样品数量',
    sample_specification VARCHAR(50) COMMENT '样品规格',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出厂检验记录';

-- 创建 transport_sale 表（储运销售）
CREATE TABLE transport_sale (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    batch_number VARCHAR(50) NOT NULL COMMENT '批号',
    environment_temperature DECIMAL(5,2) COMMENT '环境温度（℃）',
    product_temperature DECIMAL(5,2) COMMENT '产品温度（℃）',
    time DATETIME COMMENT '记录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='储运销售记录';

-- 创建 complaint 表（投诉信息）
CREATE TABLE complaint (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    complaint_reason TEXT COMMENT '投诉原因',
    complaint_time DATETIME COMMENT '投诉时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉信息表';

-- 插入产品数据
INSERT INTO product (anti_fake_code, name, specification, batch_number, production_date, shelf_life, image_url, contact_phone, contact_email) VALUES
('AF202400012345', '有机纯牛奶', '250ml/盒', 'BATCH20240101', '2024-01-15', '6个月', '/img/products/placeholder.svg', '18788919351', '2896114330@qq.com'),
('AF202400067890', '有机橄榄油', '500ml/瓶', 'BATCH20240201', '2024-02-01', '18个月', '/img/products/placeholder.svg', '18788919351', '2896114330@qq.com'),
('AF202400099999', '有机蜂蜜', '350g/瓶', 'BATCH20240301', '2024-03-10', '24个月', '/img/products/placeholder.svg', '18788919351', '2896114330@qq.com');

SELECT SLEEP(0.1);

SELECT * FROM product;

-- 原料采购
INSERT INTO material_purchase (product_name, batch_number, material_name, producer_name, producer_address) VALUES
('有机纯牛奶', 'BATCH20240101', '有机生牛乳', '绿源有机牧场', '内蒙古呼和浩特市和林格尔县'),
('有机纯牛奶', 'BATCH20240101', '维生素D3', '华药生物科技', '河北省石家庄市'),
('有机橄榄油', 'BATCH20240201', '有机橄榄果', '地中海橄榄庄园', '西班牙安达卢西亚'),
('有机蜂蜜', 'BATCH20240301', '有机蜂蜜原料', '秦岭深山养蜂基地', '陕西省汉中市');

-- 贮存记录
INSERT INTO storage (product_name, batch_number, storage_time, outbound_time, quantity, unit) VALUES
('有机纯牛奶', 'BATCH20240101', '2024-01-16 08:00:00', '2024-01-18 14:00:00', 10000, '盒'),
('有机橄榄油', 'BATCH20240201', '2024-02-05 10:00:00', '2024-02-07 09:00:00', 5000, '瓶'),
('有机蜂蜜', 'BATCH20240301', '2024-03-12 08:30:00', '2024-03-14 16:00:00', 3000, '瓶');

-- 出厂检验
INSERT INTO inspection (product_name, batch_number, sample_name, sample_quantity, sample_specification) VALUES
('有机纯牛奶', 'BATCH20240101', '有机纯牛奶', 50, '250ml/盒'),
('有机橄榄油', 'BATCH20240201', '有机橄榄油', 30, '500ml/瓶'),
('有机蜂蜜', 'BATCH20240301', '有机蜂蜜', 20, '350g/瓶');

-- 储运销售
INSERT INTO transport_sale (product_name, batch_number, environment_temperature, product_temperature, time) VALUES
('有机纯牛奶', 'BATCH20240101', 4.0, 2.5, '2024-01-20 10:00:00'),
('有机橄榄油', 'BATCH20240201', 18.0, 15.0, '2024-02-10 14:00:00'),
('有机蜂蜜', 'BATCH20240301', 22.0, 20.0, '2024-03-15 11:00:00');

-- 投诉信息
INSERT INTO complaint (product_name, complaint_reason, complaint_time) VALUES
('有机纯牛奶', '包装轻微破损', '2024-02-01 09:30:00');

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;