# ADR-0003: V2 全量覆盖 Material/MaterialPurchase 及删除决策

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
