# ADR-0002: 区块链式轻量化数据防篡改架构

采用独立区块链日志表（blockchain_log）+ RSA 数字签名 + 多链拓扑实现数据防篡改。
本设计不影响现有业务表和查询逻辑，区块仅用于事后完整性校验。

## 核心决策

1. **多链拓扑**：物料主链（material + material_purchase 按 CREATE/UPDATE 时间顺序链接，全局单链）+ 批次链（每个 production_batch 独立一条链，依次链接 storage/inspection/transport_sale）。两条链独立增长，通过跨链引用关联。

    - action 枚举仅限 `CREATE` / `UPDATE`，`DELETE` 不是合法 action（物理删除已全面禁止，见 ADR-0003）
    - 软删除走 `UPDATE` action，通过 `data_snapshot` 中的 `is_deleted: true` 标识

2. **跨链引用（Cross-chain Reference）**：批次链的创世块（production_batch 创建时）记录物料主链的最新 current_hash，证明该批次创建时所引用的物料数据状态。不复制实体数据，仅存哈希引用。

3. **事件驱动区块追加**：区块追加通过领域事件监听器（@TransactionalEventListener）实现，与主业务操作解耦。主事务失败不影响链；监听器事务失败不阻塞主业务。

4. **RSA 数字签名**：每个区块的 current_hash 用服务器 RSA 私钥签名，公钥对外暴露。管理员即使重算整条链的哈希也无法伪造签名。

5. **外部锚定**：每日凌晨将每条链最新 hash 写入 append-only 日志文件 + blockchain_anchor 表，随系统备份归档。

## 受影响的组件

- 新建：BlockchainLog 实体、BlockchainAnchor 实体、BlockchainService、BlockchainAnchorService、RsaKeyConfig
- 新建：TransportSaleRecorded 领域事件 + TransportSaleApplicationService
- 新增监听器：BatchCreatedEventListener 追加批次链创世块 + 跨链引用
- 新增监听器：GoodsReceivedEventListener、InspectionCompletedEventListener 追加对应区块
- 新建监听器：TransportSaleRecordedEventListener 追加运输销售区块
- 修改：MaterialPurchaseServiceImpl、MaterialServiceImpl、ProductionBatchServiceImpl 直接调用 appendBlock（V1 过渡方案）

## 架构的合理性

- 哈希链阻止单条记录篡改
- 数字签名阻止批量重算哈希
- 外部锚定阻止整条链替换
- 独立 blockchain_log 表不侵入原有业务表
- 领域事件监听器模式与现有 DDD 架构一致
