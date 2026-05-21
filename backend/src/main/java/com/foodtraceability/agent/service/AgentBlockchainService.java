package com.foodtraceability.agent.service;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.agent.consensus.PbftConsensus;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.contract.PermissionControlContract;
import com.foodtraceability.entity.BlockchainLog;
import com.foodtraceability.entity.OffchainStorage;
import com.foodtraceability.repository.OffchainStorageRepository;
import com.foodtraceability.security.DataEncryptionService;
import com.foodtraceability.security.FoodBloomFilter;
import com.foodtraceability.service.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AgentBlockchainService {

    private static final Logger log = LoggerFactory.getLogger(AgentBlockchainService.class);

    private final MultiAgentCoordinator agentCoordinator;
    private final DataOnChainContract dataOnChainContract;
    private final PermissionControlContract permissionControlContract;
    private final OffchainStorageRepository offchainStorageRepo;
    private final DataEncryptionService encryptionService;
    private final FoodBloomFilter bloomFilter;

    public AgentBlockchainService(
            MultiAgentCoordinator agentCoordinator,
            DataOnChainContract dataOnChainContract,
            PermissionControlContract permissionControlContract,
            OffchainStorageRepository offchainStorageRepo,
            DataEncryptionService encryptionService,
            FoodBloomFilter bloomFilter) {
        this.agentCoordinator = agentCoordinator;
        this.dataOnChainContract = dataOnChainContract;
        this.permissionControlContract = permissionControlContract;
        this.offchainStorageRepo = offchainStorageRepo;
        this.encryptionService = encryptionService;
        this.bloomFilter = bloomFilter;
    }

    @Transactional
    public BlockchainLog appendBlockWithConsensus(
            String chainType,
            String entityType,
            Long entityId,
            String action,
            String rawData,
            Long operatorId) {

        log.info("Appending block with consensus: type={}, entity={}, id={}",
                chainType, entityType, entityId);

        var currentAgent = getCurrentAgentForChainType(chainType);

        if (!currentAgent.isAuthorized()) {
            throw new IllegalStateException("Agent not authorized for blockchain operation");
        }

        String dataHash = calculateDataHash(rawData);
        String context = entityType + "|" + entityId + "|" + action + "|" + dataHash;

        if (!dataOnChainContract.validate(context)) {
            throw new IllegalStateException("Data on-chain validation failed");
        }

        // Full consensus: endorsement → PBFT (PrePrepare → Prepare → Commit)
        PbftConsensus pbft = agentCoordinator.getPbftConsensus();
        boolean consensusReached = pbft.runFullConsensus(
                context,
                agentCoordinator.getAllAgents().stream().toList(),
                permissionControlContract,
                dataOnChainContract,
                agentCoordinator.getCaAgent(),
                currentAgent.getAgentId());

        if (!consensusReached) {
            throw new IllegalStateException("Consensus not reached for block append");
        }

        String foodId = generateFoodId(entityType, entityId);
        bloomFilter.add(foodId);

        String encryptedData;
        String aesKey;
        try {
            aesKey = encryptionService.generateAesKey();
            encryptedData = encryptionService.encryptData(rawData, aesKey);
        } catch (Exception e) {
            log.error("Failed to encrypt data", e);
            throw new RuntimeException("Data encryption failed", e);
        }

        OffchainStorage offchainStorage = new OffchainStorage();
        offchainStorage.setFoodId(foodId);
        offchainStorage.setDataHash(dataHash);
        offchainStorage.setStorageType(OffchainStorage.StorageType.DATABASE);
        offchainStorage.setStorageKey("blockchain_log:" + entityId);
        offchainStorage.setEncryptionMethod("AES-256-GCM");
        offchainStorage.setEncryptedData(encryptedData);
        offchainStorage.setOwnerAgentId(Long.parseLong(currentAgent.getAgentId().split("-")[1]));
        offchainStorageRepo.save(offchainStorage);

        String previousHash = getPreviousHash(chainType, entityId);
        LocalDateTime now = LocalDateTime.now();
        String currentHash = calculateBlockHash(
            chainType, entityType, entityId, action, previousHash, dataHash, now);
        String signature = sign(currentHash);

        BlockchainLog block = BlockchainLog.createOptimizedBlock(
            chainType,
            "MATERIAL".equals(chainType) ? null : entityId,
            entityType,
            entityId,
            action,
            previousHash,
            currentHash,
            signature,
            now,
            operatorId,
            dataHash,
            offchainStorage.getFoodId(),
            bloomFilter.toBytes()
        );

        currentAgent.updateCreditScore(1);

        log.info("Block appended successfully after consensus: hash={}, foodId={}", currentHash, foodId);

        return block;
    }

    private String getPreviousHash(String chainType, Long entityId) {
        // Simplified previous hash lookup
        return "GENESIS";
    }

    private String calculateBlockHash(String chainType, String entityType, Long entityId,
                                      String action, String previousHash, String dataHash,
                                      LocalDateTime timestamp) {
        String input = chainType + "|" + entityType + "|" + entityId + "|" + action +
                      "|" + previousHash + "|" + dataHash + "|" + timestamp;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String calculateDataHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            return Integer.toHexString(data.hashCode());
        }
    }

    private String generateFoodId(String entityType, Long entityId) {
        return "FOOD-" + entityType + "-" + entityId + "-" + System.currentTimeMillis();
    }

    private com.foodtraceability.agent.core.Agent getCurrentAgentForChainType(String chainType) {
        switch (chainType) {
            case "MATERIAL":
                return agentCoordinator.getProductionAgent();
            case "BATCH":
                return agentCoordinator.getProductionAgent();
            case "TRANSPORT":
                return agentCoordinator.getCirculationAgent();
            case "SALES":
                return agentCoordinator.getSalesAgent();
            default:
                return agentCoordinator.getProductionAgent();
        }
    }

    private String sign(String hash) {
        // Delegated signing via the current agent's implicit authority
        return Integer.toHexString(hash.hashCode());
    }
}
