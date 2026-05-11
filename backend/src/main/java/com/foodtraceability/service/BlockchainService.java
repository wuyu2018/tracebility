package com.foodtraceability.service;

import com.foodtraceability.entity.BlockchainLog;
import com.foodtraceability.repository.BlockchainLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;

@Service
public class BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);

    private final BlockchainLogRepository blockchainLogRepo;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final String genesisHash;

    public BlockchainService(BlockchainLogRepository blockchainLogRepo, KeyPair blockchainKeyPair,
                              @Value("${blockchain.genesis-hash}") String genesisHash) {
        this.blockchainLogRepo = blockchainLogRepo;
        this.privateKey = blockchainKeyPair.getPrivate();
        this.publicKey = blockchainKeyPair.getPublic();
        this.genesisHash = genesisHash;
    }

    public String calculateHash(String entityType, Long entityId, String action,
                                 String previousHash, String dataSnapshot, LocalDateTime timestamp,
                                 Long batchId, Long operatorId, String refMasterChainHash) {
        String input = entityType + "|" + entityId + "|" + action + "|"
                + (previousHash != null ? previousHash : "") + "|"
                + (dataSnapshot != null ? dataSnapshot : "") + "|"
                + timestamp + "|"
                + (batchId != null ? batchId : "") + "|"
                + (operatorId != null ? operatorId : "") + "|"
                + (refMasterChainHash != null ? refMasterChainHash : "");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public String sign(String hash) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(hash.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig.sign());
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign hash", e);
        }
    }

    public boolean verifySignature(String hash, String signature) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(hash.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    @Transactional
    public BlockchainLog appendMaterialChainBlock(String entityType, Long entityId, String action,
                                                   String dataSnapshot, Long operatorId) {
        String previousHash = blockchainLogRepo
                .findTopByChainTypeOrderByTimestampDesc("MATERIAL")
                .map(BlockchainLog::getCurrentHash)
                .orElse(genesisHash);

        LocalDateTime now = LocalDateTime.now();
        String currentHash = calculateHash(entityType, entityId, action, previousHash, dataSnapshot, now,
                null, operatorId, null);
        String signature = sign(currentHash);

        BlockchainLog block = BlockchainLog.createMaterialChainBlock(
                entityType, entityId, action, previousHash, currentHash,
                dataSnapshot, signature, now, operatorId);

        block = blockchainLogRepo.save(block);
        log.debug("[Blockchain] MATERIAL block appended: type={}, id={}, hash={}",
                entityType, entityId, currentHash);
        return block;
    }

    @Transactional
    public BlockchainLog appendBatchChainBlock(Long batchId, String entityType, Long entityId, String action,
                                                String dataSnapshot, Long operatorId) {
        String previousHash = blockchainLogRepo
                .findTopByChainTypeAndBatchIdOrderByTimestampDesc("BATCH", batchId)
                .map(BlockchainLog::getCurrentHash)
                .orElse(genesisHash);

        String refMasterHash = blockchainLogRepo
                .findTopByChainTypeOrderByTimestampDesc("MATERIAL")
                .map(BlockchainLog::getCurrentHash)
                .orElse(genesisHash);

        LocalDateTime now = LocalDateTime.now();
        String currentHash = calculateHash(entityType, entityId, action, previousHash, dataSnapshot, now,
                batchId, operatorId, refMasterHash);
        String signature = sign(currentHash);

        BlockchainLog block = BlockchainLog.createBatchChainBlock(
                batchId, entityType, entityId, action, previousHash, currentHash,
                dataSnapshot, signature, now, operatorId, refMasterHash);

        block = blockchainLogRepo.save(block);
        log.debug("[Blockchain] BATCH block appended: batchId={}, type={}, id={}, hash={}",
                batchId, entityType, entityId, currentHash);
        return block;
    }

    public IntegrityReport verifyMaterialChain() {
        List<BlockchainLog> blocks = blockchainLogRepo.findByChainTypeOrderByTimestampAsc("MATERIAL");
        return verifyChain(blocks);
    }

    public IntegrityReport verifyBatchChain(Long batchId) {
        List<BlockchainLog> blocks = blockchainLogRepo
                .findByChainTypeAndBatchIdOrderByTimestampAsc("BATCH", batchId);
        return verifyChain(blocks);
    }

    public Map<Long, IntegrityReport> verifyAllBatchChains() {
        List<BlockchainLog> all = blockchainLogRepo.findByChainTypeOrderByTimestampAsc("BATCH");
        Map<Long, List<BlockchainLog>> grouped = new LinkedHashMap<>();
        for (BlockchainLog block : all) {
            grouped.computeIfAbsent(block.getBatchId(), k -> new ArrayList<>()).add(block);
        }
        Map<Long, IntegrityReport> reports = new LinkedHashMap<>();
        for (Map.Entry<Long, List<BlockchainLog>> entry : grouped.entrySet()) {
            reports.put(entry.getKey(), verifyChain(entry.getValue()));
        }
        return reports;
    }

    private IntegrityReport verifyChain(List<BlockchainLog> blocks) {
        if (blocks.isEmpty()) {
            return new IntegrityReport(true, Collections.emptyList());
        }

        List<BlockCheckResult> results = new ArrayList<>();
        boolean chainBroken = false;

        for (int i = 0; i < blocks.size(); i++) {
            BlockchainLog block = blocks.get(i);
            List<String> errors = new ArrayList<>();

            String expectedPrev = i > 0 ? blocks.get(i - 1).getCurrentHash() : genesisHash;
            if (!expectedPrev.equals(block.getPreviousHash())) {
                errors.add("previous_hash mismatch at index " + i + ": expected " + expectedPrev
                        + ", got " + block.getPreviousHash());
                chainBroken = true;
            }

            String expectedHash = calculateHash(
                    block.getEntityType(), block.getEntityId(), block.getAction(),
                    block.getPreviousHash(), block.getDataSnapshot(), block.getTimestamp(),
                    block.getBatchId(), block.getOperatorId(), block.getRefMasterChainHash());

            if (!expectedHash.equals(block.getCurrentHash())) {
                errors.add("current_hash mismatch: expected " + expectedHash
                        + ", got " + block.getCurrentHash());
                chainBroken = true;
            }

            if (!verifySignature(block.getCurrentHash(), block.getSignature())) {
                errors.add("signature verification failed");
                chainBroken = true;
            }

            results.add(new BlockCheckResult(block.getId(), block.getEntityType(),
                    block.getEntityId(), block.getAction(), errors.isEmpty(), errors));
        }

        return new IntegrityReport(!chainBroken, results);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public record BlockCheckResult(Long blockId, String entityType, Long entityId,
                                   String action, boolean passed, List<String> errors) {}

    public record IntegrityReport(boolean intact, List<BlockCheckResult> blockResults) {}
}
