-- ============================================
-- 数据库迁移脚本：支持链上链下分离存储
-- 基于第二篇论文的数据存储优化方案
-- 日期：2026-05-20
-- ============================================

-- 1. 新增 offchain_storage 表 (链下存储)
CREATE TABLE IF NOT EXISTS offchain_storage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    food_id VARCHAR(100) NOT NULL UNIQUE COMMENT '食品唯一标识',
    data_hash VARCHAR(64) NOT NULL COMMENT '原始数据 SHA-256 哈希',
    storage_type VARCHAR(20) NOT NULL COMMENT '存储类型：REDIS/LOCAL_FILE/OSS/DATABASE',
    storage_key VARCHAR(200) NOT NULL COMMENT '存储位置/键',
    encryption_method VARCHAR(50) COMMENT '加密算法：AES-256-GCM',
    encrypted_data TEXT COMMENT '加密后的数据',
    encrypted_aes_key TEXT COMMENT 'RSA 加密的 AES 密钥',
    owner_agent_id BIGINT NOT NULL COMMENT '数据所有者 Agent ID',
    access_policy JSON COMMENT '访问控制策略 (JSON)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expires_at DATETIME COMMENT '过期时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
    INDEX idx_offchain_food_id (food_id),
    INDEX idx_offchain_storage (storage_type, storage_key),
    INDEX idx_offchain_owner (owner_agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链下加密存储表';

-- 2. 修改 blockchain_log 表，新增字段
ALTER TABLE blockchain_log 
ADD COLUMN IF NOT EXISTS data_hash VARCHAR(64) COMMENT '数据摘要 SHA-256' AFTER data_snapshot,
ADD COLUMN IF NOT EXISTS offchain_ref VARCHAR(200) COMMENT '链下存储引用' AFTER data_hash,
ADD COLUMN IF NOT EXISTS bloom_filter BLOB COMMENT 'Bloom Filter 二进制数据' AFTER offchain_ref,
ADD COLUMN IF NOT EXISTS metadata_index JSON COMMENT '元数据索引 (JSON)' AFTER bloom_filter;

-- 3. 为 blockchain_log 添加新索引优化查询
CREATE INDEX IF NOT EXISTS idx_bl_chain_type ON blockchain_log(chain_type);
CREATE INDEX IF NOT EXISTS idx_bl_entity ON blockchain_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_bl_data_hash ON blockchain_log(data_hash);

-- 4. 可选：创建分区表 (如果数据量较大)
-- 注意：生产环境使用前请先备份数据
-- ALTER TABLE blockchain_log 
-- PARTITION BY RANGE (YEAR(timestamp)) (
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p2026 VALUES LESS THAN (2027),
--     PARTITION p2027 VALUES LESS THAN (2028),
--     PARTITION pmax VALUES LESS THAN MAXVALUE
-- );

-- 5. 智能合约状态表 (为后续智能合约功能预留)
CREATE TABLE IF NOT EXISTS smart_contract_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    contract_id VARCHAR(50) NOT NULL COMMENT '合约 ID',
    contract_type VARCHAR(50) NOT NULL COMMENT '合约类型：PERMISSION/TRANSACTION/DATA/STATE',
    state_key VARCHAR(200) NOT NULL COMMENT '状态键',
    state_value TEXT COMMENT '状态值',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '版本号 (乐观锁)',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_contract (contract_id, state_key),
    INDEX idx_contract_type (contract_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能合约状态表';

-- 6. Agent 身份表 (为后续 Agent 管理预留)
CREATE TABLE IF NOT EXISTS agent_identity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
    agent_id VARCHAR(50) NOT NULL UNIQUE COMMENT 'Agent 唯一标识',
    agent_type VARCHAR(30) NOT NULL COMMENT 'Agent 类型：PRODUCTION/CIRCULATION/SALES/CA',
    certificate_serial VARCHAR(100) COMMENT '证书序列号',
    public_key TEXT COMMENT 'RSA 公钥',
    credit_score BIGINT NOT NULL DEFAULT 100 COMMENT '信誉评分',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/SUSPENDED/REVOKED',
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_active_at DATETIME COMMENT '最后活跃时间',
    metadata JSON COMMENT '元数据',
    INDEX idx_agent_type (agent_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 身份表';

-- 7. 视图：完整的食品溯源信息 (链上 + 链下)
CREATE OR REPLACE VIEW v_food_traceability AS
SELECT 
    bl.id AS blockchain_log_id,
    bl.chain_type,
    bl.entity_type,
    bl.entity_id,
    bl.action,
    bl.current_hash,
    bl.data_hash,
    bl.offchain_ref AS food_id,
    bl.timestamp,
    os.storage_type,
    os.storage_key,
    os.encryption_method,
    os.owner_agent_id,
    os.access_policy,
    os.created_at AS storage_created_at
FROM blockchain_log bl
LEFT JOIN offchain_storage os ON bl.offchain_ref = os.food_id AND os.is_deleted = 0
ORDER BY bl.timestamp DESC;

-- ============================================
-- 数据迁移说明
-- ============================================
-- 
-- 旧数据迁移策略:
-- 1. 对于已有的 blockchain_log 数据，data_snapshot 字段保持不变
-- 2. 新增的数据请使用优化后的存储方案
-- 3. 可选择性地将旧数据的 data_snapshot 迁移到 offchain_storage
-- 
-- 回滚脚本:
-- 如需回滚，请执行以下步骤:
-- 1. DROP TABLE IF EXISTS offchain_storage;
-- 2. DROP TABLE IF EXISTS smart_contract_state;
-- 3. DROP TABLE IF EXISTS agent_identity;
-- 4. DROP VIEW IF EXISTS v_food_traceability;
-- 5. ALTER TABLE blockchain_log DROP COLUMN data_hash, DROP COLUMN offchain_ref, DROP COLUMN bloom_filter, DROP COLUMN metadata_index;
-- 
-- 性能建议:
-- 1. 当 blockchain_log 数据超过 100 万行时，建议启用分区表
-- 2. 定期清理 is_deleted = 1 的过期数据
-- 3. 为 hot 数据配置 Redis 缓存
