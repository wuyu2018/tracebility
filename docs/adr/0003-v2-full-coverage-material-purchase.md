#  

基于 grilling 会话（`类区块链轻量化数据防篡改-第二阶段计划`）的共识。

## 核心决策

1. **复用 V1 JPA 实体**：Material/MaterialPurchase 的 V2 ApplicationService 直接引用 `com.foodtraceability.entity.Material` / `MaterialPurchase`，不创建 `traceability/domain/entity/` 层，与现有 `ProductionBatchApplicationService` 模式一致。

2. **Event 发布模式**：遵循 `publishAfterCommit()`（`TransactionSynchronizationManager` + `DomainEventPublisherImpl`），与现有 V2 模式一致。

3. **软删上链**：
   - `deleteMaterial()` → `MaterialUpdated` 事件 → `BlockchainService.appendMaterialChainBlock("MATERIAL", id, "UPDATE", snapshot, null)`，data_snapshot 含 `is_deleted: true`
   - `deleteMaterialPurchase()` → `MaterialPurchaseUpdated` 事件 → `BlockchainService.appendMaterialChainBlock("MATERIAL_PURCHASE", id, "UPDATE", snapshot, null)`，data_snapshot 含 `is_deleted: true`

4. **物理删除：永久禁止**。应用层拦截所有 `DELETE` 请求，无对应区块类型。

5. **冻结规则**：已软删实体不再接受后续业务更新（`update` 抛出 `BusinessException`）。

6. **activate/deactivate**：作为 `updateMaterial(isActive)` 的统一入口，不另建独立服务方法。

7. **action 枚举**：仅 `CREATE` / `UPDATE`，`DELETE` 不是合法 action——物理删除已全面禁止，软删除通过 `data_snapshot` 中的 `is_deleted: true` 标识。

## 受影响文件

- CONTEXT.md：新增删除规则表、物料主链定义补充 SOFT_DELETE
- ADR-0002：action 枚举说明补充（仅 CREATE/UPDATE）
- `docs/类区块链轻量化数据防篡改-第二阶段计划.md`：Step 1/2 补充冻结校验、soft_delete 标记

所有 6 个决策点已确认。以下是最终的完整方案概览。
---
最终阶段二计划（决策汇总）
术语修正
V2 路径名修复：
V1（错误）                V2（修正）
/api/materials             → /api/v2/material-purchases        ← 采购单
/api/material-varieties    → /api/v2/material-varieties        ← 物料品种（不变，正确）
新建文件（~28 个）
traceability/
├── domain/
│   ├── event/
│   │   ├── MaterialChanged.java            ← action: CREATE|UPDATE|ACTIVATE|DEACTIVATE
│   │   ├── MaterialPurchaseChanged.java    ← action: CREATE|UPDATE|SOFT_DELETE
│   │   └── ComplaintCreated.java           ← 入事件日志，不入链
│   └── complaint/                          ← 独立子域
│       ├── Complaint.java                  ← 领域实体（或复用现有 entity）
│       └── ComplaintRepository.java        ← 领域仓储接口
├── application/
│   ├── dto/
│   │   ├── CreateMaterialRequest.java
│   │   ├── UpdateMaterialRequest.java
│   │   ├── MaterialResponse.java
│   │   ├── CreateMaterialPurchaseRequest.java
│   │   ├── UpdateMaterialPurchaseRequest.java
│   │   ├── MaterialPurchaseResponse.java
│   │   ├── CreateComplaintRequest.java
│   │   └── ComplaintResponse.java
│   ├── event/
│   │   ├── MaterialChangedEventListener.java         ← appendMaterialChainBlock
│   │   ├── MaterialPurchaseChangedEventListener.java ← appendMaterialChainBlock
│   │   └── ComplaintCreatedEventListener.java        ← 仅日志
│   └── service/
│       ├── MaterialApplicationService.java
│       ├── MaterialPurchaseApplicationService.java
│       └── complaint/
│           └── ComplaintApplicationService.java
└── interfaces/
    ├── rest/
    │   ├── MaterialVarietyController.java     ← /api/v2/material-varieties
    │   ├── MaterialPurchaseController.java    ← /api/v2/material-purchases
    │   └── complaint/
    │       └── ComplaintController.java       ← /api/v2/complaints
    └── dto/
        ├── (对应每个接口的 request/response 约 9 个)
        └── complaint/
            ├── CreateComplaintRequest.java
            └── ComplaintResponse.java
删除的 V1 文件
文件
service/impl/MaterialServiceImpl.java
service/impl/MaterialPurchaseServiceImpl.java
修改文件
文件
service/impl/ProductionBatchServiceImpl.java
controller/DataManagementController.java
frontend/src/services/api.js
保留 V1 不变的部分
- 防伪码生成/列表/导出（管理员工具）
- 产品 CRUD + 产品-原料可见性
- 管理员认证（登录/captcha）
- 数据导入工具（/api/insert/*）
- ProductionBatchServiceImpl 中的查询/update/delete 方法
- 所有 GET 查询端点
区块链覆盖
操作
Material CREATE/UPDATE/ACTIVATE/DEACTIVATE
MaterialPurchase CREATE/UPDATE/SOFT_DELETE
Complaint CREATE
---