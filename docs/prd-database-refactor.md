# PRD: 数据库重构 — 原料独立化与删除语义分离

## Problem Statement

当前数据库设计将 MaterialPurchase 通过外键直接绑定到 Product，导致两个严重问题：

1. **共享原料被级联误删**：当一个 Product 被删除时，级联删除会清除该 Product 关联的所有 MaterialPurchase 记录。但同一个原料批次可能被多个 ProductionBatch 引用——删除一个 Product 会破坏其他 Product 的溯源链路。
2. **硬删除破坏历史追溯**：溯源系统需要保留已售商品的完整链路以备追责。当前 `deleteProduct()` 执行物理 DELETE，连带清除 ProductionBatch、SecurityCode、Storage 等全部关联记录，已售商品的扫码查询将返回空。

此外，Material 和 MaterialPurchase 未分离（品种 vs 采购批次），Complaint 使用冗余字符串字段而非外键关联，均为设计缺陷。

## Solution

重构数据库模型，核心变更：

1. **Material 独立为品种表**，MaterialPurchase 仅记录采购批次，通过外键指向 Material。Material 不再绑定 Product。
2. **引入 ProductMaterialRelation 中间表**，在关联上控制可见性（is_hidden）。产品删除时只清除此关联行，不触及其他数据。
3. **删除语义分离**：产品有绑定 Batch/SecurityCode 时只能软删，无绑定时可物理清除。软删仅标记 Product.is_deleted=true 并清理 ProductMaterialRelation。
4. **Complaint 改外键关联**：从冗余字符串字段改为指向 SecurityCode。

## User Stories

1. As a 管理员, I want to 将原料品种与采购批次分离管理, so that 同一种原料可以被多个产品共享使用
2. As a 管理员, I want to 为产品配置可见的原料品种列表, so that 每个产品只看到与自己相关的原料
3. As a 管理员, I want to 停用产品时保留所有生产批次和防伪码, so that 已售商品仍可正常扫码溯源
4. As a 管理员, I want to 物理删除无任何批次和防伪码关联的产品, so that 错误录入的产品可以彻底清理
5. As a 管理员, I want to 创建生产批次时自动校验所选原料对该产品可见, so that 避免误选未授权原料导致脏数据
6. As a 管理员, I want to 通过管理页面管理原料品种（增/删/改/查/启用/停用）, so that 原料库可维护
7. As a 管理员, I want to 查看某个产品当前关联了哪些原料品种及其可见性状态, so that 了解产品原料配置
8. As a 管理员, I want to 提交投诉时自动关联到防伪码记录, so that 投诉可溯源到具体产品/批次
9. As a 管理员, I want to 查询投诉时看到对应的产品和批次信息, so that 定位问题批次
10. As a 管理员, I want to 停用产品后在管理后台默认不显示其批次, so that 工作台不混乱
11. As a 管理员, I want to 有开关控制是否显示已停产产品的批次, so that 需要时仍可查看历史
12. As a 消费者, I want to 扫码查询已停产产品的信息, so that 了解仍在保质期内的商品详情
13. As a 管理员, I want to 删除原料时仅通过 ProductMaterialRelation 解除可见性, so that 不影响其他产品的原料引用
14. As a 开发者, I want to 有统一的 DeletionPolicy 模块处理删除逻辑, so that 删除规则集中管理、可测试
15. As a 开发者, I want to 有 BatchMaterialValidator 校验批次原料授权, so that 数据一致性约束不依赖前端

## Implementation Decisions

### 模块划分

**新建模块：**
- **Material 实体/仓库/服务**：原料品种 CRUD。字段：id, name（唯一索引）, isActive, createdAt, updatedAt。浅模块。
- **ProductMaterialRelation 实体/仓库/服务**：产品-原料可见性管理。字段：id, productId, materialId, isHidden。UNIQUE(productId, materialId)。浅模块。
- **DeletionPolicy**：封装所有删除规则。接口：`deleteProduct(id)`, `hardDeleteProduct(id)`, `deleteMaterial(id)`。内部处理关联检查、软/硬删分支。深模块。
- **BatchMaterialValidator**：校验批次原料授权。接口：`validate(batchId, materialPurchaseId)`。内部查询 ProductMaterialRelation.is_hidden。深模块。

**修改模块：**
- **MaterialPurchase 实体**：删除 `product` （@ManyToOne Product）和 `materialName` 字段。添加 `material`（@ManyToOne Material）外键。
- **BatchMaterialRelation 实体**：主键改为复合主键（batchId, materialPurchaseId）。关联属性 `material` 重命名为 `materialPurchase`。
- **Complaint 实体**：删除 `antiFakeCode`、`productName`、`batchNumber` 冗余字段。添加 `securityCode`（@ManyToOne SecurityCode）。
- **ProductService**：`deleteProduct()` → 软删逻辑（is_deleted=true + 清理 ProductMaterialRelation）+ 物理删逻辑（无关联时 DELETE）。调用 DeletionPolicy。
- **DataManagementController**：新增 Material CRUD 端点、ProductMaterialRelation 管理端点。新增"清除产品"（物理删）端点。
- **TraceabilityService**：溯源链不变（BatchMaterialRelation 结构不变），微调 Complaint 关联方式。
- **前端 Vue 组件**：原料管理页适配 Material/MaterialPurchase 分离。产品管理页增加原料可见性配置。

### Schema 变更

完整 DDL 见 `docs/adr/0001-database-refactor-new-schema.md`。核心变更：

- 新建 `material` 表，`product_material_relation` 表
- `material_purchase` 删 `product_id` + `material_name`，加 `material_id`
- `batch_material_relation` 改复合主键
- `complaint` 改 `security_code_id` 外键
- 所有子表从级联删除改为 RESTRICT（由应用层控制）
- 开发环境采用 B 方案：直接跑新 DDL 重建，不计现有测试数据

### 接口设计

**新增端点：**
- `POST/GET /api/materials` — 原料品种 CRUD
- `GET /api/materials/{id}` — 单个品种详情
- `DELETE /api/materials/{id}` — 删除（走 DeletionPolicy）
- `POST /api/product-materials` — 绑定产品与原料（设置可见性）
- `GET /api/product-materials?productId={id}` — 查询产品关联的原料
- `PATCH /api/product-materials/{id}/visibility` — 切换 is_hidden
- `DELETE /api/products/{id}/hard` — 物理删除产品（仅无关联时可用）

**修改端点：**
- `DELETE /api/products/{id}` — 改为软删 + 清理 ProductMaterialRelation
- `POST /api/batches` — 内部集成 BatchMaterialValidator 校验

## Testing Decisions

### 测试原则

只测外部行为，不测实现细节。对给定输入验证输出/状态/副作用，不验证内部调用顺序或私有方法。

### 测试范围

| 模块 | 测试类型 | 覆盖要点 |
|---|---|---|
| DeletionPolicy | 单元测试 | 软删 Product（有 Batch/Code）、物理删 Product（无关联）、删除 Material、拒绝删除有绑定的 Product |
| BatchMaterialValidator | 单元测试 | 通过校验（is_hidden=false）、拒绝（is_hidden=true）、Material 不存在、ProductMaterialRelation 不存在 |
| ProductService | 集成测试 | 创建/软删/物理删产品的完整流，关联表状态验证 |
| MaterialService | 集成测试 | CRUD，is_active 切换，被关联时删除行为 |
| ProductMaterialRelationService | 集成测试 | 绑定/解绑，is_hidden 切换 |
| ComplaintService | 集成测试 | 通过 antiFakeCode 创建投诉，自动关联 SecurityCode |

### Prior art

当前代码库无测试文件（无 `src/test/` 目录）。测试框架推荐 JUnit 5 + Spring Boot Test（@SpringBootTest）用于集成测试，Mockito 用于单元测试。测试数据通过 `@Sql` 注解或 Testcontainers 管理。

## Out of Scope

- **数据清理入口**（管理员批量清除僵尸数据的 UI/API）。仅实现基础的"无关联可物理删"规则，批量清理留给后续迭代。
- **前端全面重写**。仅修改必要的原料管理页和产品可见性配置页。布局和交互风格保持现有。
- **权限控制细化**。本次不引入角色/权限系统（如仅管理员可物理删除），保留现有简单认证。
- **旧数据迁移脚本**。采用 B 方案（开发环境重建），不生产生产环境迁移脚本。

## Further Notes

- 此 PRD 对应 ADR-0001（`docs/adr/0001-database-refactor-new-schema.md`），DDL 细节见该文档。
- CONTEXT.md 已更新领域语言。后续开发者应使用决议后的术语。
- 实现顺序建议：新表 DDL → Material 模块 → ProductMaterialRelation 模块 → DeletionPolicy → BatchMaterialValidator → 修改现有 Service → 前端适配
