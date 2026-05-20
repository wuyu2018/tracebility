package com.foodtraceability.service;

import com.foodtraceability.entity.BlockchainLog;
import com.foodtraceability.repository.BlockchainLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BlockchainRepairService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainRepairService.class);

    private final BlockchainLogRepository blockchainLogRepo;
    private final BlockchainService blockchainService;
    private final String genesisHash;

    public BlockchainRepairService(BlockchainLogRepository blockchainLogRepo,
                                    BlockchainService blockchainService,
                                    @Value("${blockchain.genesis-hash}") String genesisHash) {
        this.blockchainLogRepo = blockchainLogRepo;
        this.blockchainService = blockchainService;
        this.genesisHash = genesisHash;
    }

    @Transactional
    public Map<String, Object> repairAll() {
        int materialFixed = repairMaterialChain();
        int batchFixed = repairBatchChains();
        int totalFixed = materialFixed + batchFixed;

        log.info("[BlockchainRepair] 修复完成: 原材料链 {} 个区块, 批次链 {} 个区块, 共 {} 个",
                materialFixed, batchFixed, totalFixed);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repaired", true);
        result.put("materialBlocksFixed", materialFixed);
        result.put("batchBlocksFixed", batchFixed);
        result.put("totalFixed", totalFixed);
        return result;
    }

    private int repairMaterialChain() {
        List<BlockchainLog> blocks = blockchainLogRepo.findByChainTypeOrderByTimestampAsc("MATERIAL");
        return repairBlocks(blocks);
    }

    private int repairBatchChains() {
        List<BlockchainLog> all = blockchainLogRepo.findByChainTypeOrderByTimestampAsc("BATCH");
        Map<Long, List<BlockchainLog>> grouped = new LinkedHashMap<>();
        for (BlockchainLog block : all) {
            grouped.computeIfAbsent(block.getBatchId(), k -> new ArrayList<>()).add(block);
        }

        int total = 0;
        for (List<BlockchainLog> chain : grouped.values()) {
            total += repairBlocks(chain);
        }
        return total;
    }

    private int repairBlocks(List<BlockchainLog> blocks) {
        if (blocks.isEmpty()) return 0;

        int fixed = 0;
        for (int i = 0; i < blocks.size(); i++) {
            BlockchainLog block = blocks.get(i);

            // 修正前序哈希：指向上一区块的最新 currentHash
            String newPrevHash = i > 0 ? blocks.get(i - 1).getCurrentHash() : genesisHash;
            block.setPreviousHash(newPrevHash);

            // 用当前所有字段值重新计算哈希
            String newHash = blockchainService.calculateHash(
                    block.getEntityType(), block.getEntityId(), block.getAction(),
                    block.getPreviousHash(), block.getDataSnapshot(), block.getTimestamp(),
                    block.getBatchId(), block.getOperatorId(), block.getRefMasterChainHash());

            // 用当前 RSA 私钥重新签名
            String newSignature = blockchainService.sign(newHash);

            block.setCurrentHash(newHash);
            block.setSignature(newSignature);
            blockchainLogRepo.save(block);
            fixed++;
        }

        log.info("[BlockchainRepair] 修复了 {} 个区块 (chainType={})", fixed,
                blocks.isEmpty() ? "?" : blocks.get(0).getChainType());
        return fixed;
    }
}
