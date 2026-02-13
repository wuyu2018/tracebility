-- data.sql 修改版本
SET FOREIGN_KEY_CHECKS = 0;

-- 先插入产品数据（主表）
INSERT INTO product (anti_fake_code, name, specification, batch_number, production_date, shelf_life, image_url, contact_phone, contact_email) VALUES
('AF202400012345', '有机纯牛奶', '250ml/盒', 'BATCH20240101', '2024-01-15', '6个月', '/img/products/placeholder.svg', '18788919351', '2896114330@qq.com'),
('AF202400067890', '有机橄榄油', '500ml/瓶', 'BATCH20240201', '2024-02-01', '18个月', '/img/products/placeholder.svg', '18788919351', '2896114330@qq.com'),
('AF202400099999', '有机蜂蜜', '350g/瓶', 'BATCH20240301', '2024-03-10', '24个月', '/img/products/placeholder.svg', '18788919351', '2896114330@qq.com');

SELECT SLEEP(0.1);

SELECT * FROM product;

-- 原料采购（假设product_id对应1,2,3）
INSERT INTO material_purchase (product_id, material_name, producer_name, producer_address) VALUES
(1, '有机生牛乳', '绿源有机牧场', '内蒙古呼和浩特市和林格尔县'),
(1, '维生素D3', '华药生物科技', '河北省石家庄市'),
(2, '有机橄榄果', '地中海橄榄庄园', '西班牙安达卢西亚'),
(3, '有机蜂蜜原料', '秦岭深山养蜂基地', '陕西省汉中市');

-- 贮存记录
INSERT INTO storage (product_id, storage_time, outbound_time, quantity, unit) VALUES
(1, '2024-01-16 08:00:00', '2024-01-18 14:00:00', 10000, '盒'),
(2, '2024-02-05 10:00:00', '2024-02-07 09:00:00', 5000, '瓶'),
(3, '2024-03-12 08:30:00', '2024-03-14 16:00:00', 3000, '瓶');

-- 出厂检验
INSERT INTO inspection (product_id, sample_name, sample_quantity, sample_specification) VALUES
(1, '有机纯牛奶', 50, '250ml/盒'),
(2, '有机橄榄油', 30, '500ml/瓶'),
(3, '有机蜂蜜', 20, '350g/瓶');

-- 储运销售
INSERT INTO transport_sale (product_id, environment_temperature, product_temperature, time) VALUES
(1, 4.0, 2.5, '2024-01-20 10:00:00'),
(2, 18.0, 15.0, '2024-02-10 14:00:00'),
(3, 22.0, 20.0, '2024-03-15 11:00:00');

-- 投诉信息
INSERT INTO complaint (product_id, complaint_reason, complaint_time) VALUES
(1, '包装轻微破损', '2024-02-01 09:30:00');

SET FOREIGN_KEY_CHECKS = 1;