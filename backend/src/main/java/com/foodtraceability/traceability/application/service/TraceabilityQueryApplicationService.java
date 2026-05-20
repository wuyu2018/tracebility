package com.foodtraceability.traceability.application.service;

import com.foodtraceability.entity.BlockchainLog;
import com.foodtraceability.repository.BlockchainLogRepository;
import com.foodtraceability.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TraceabilityQueryApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(TraceabilityQueryApplicationService.class);
    
    private final BlockchainLogRepository blockchainLogRepo;
    private final CacheService cacheService;
    
    public TraceabilityQueryApplicationService(BlockchainLogRepository blockchainLogRepo,
                                               CacheService cacheService) {
        this.blockchainLogRepo = blockchainLogRepo;
        this.cacheService = cacheService;
    }
    
    @Cacheable(value = "blockchain:log", key = "#entityType + ':' + #entityId", unless = "#result == null")
    @Transactional(readOnly = true)
    public List<BlockchainLog> queryByEntity(String entityType, Long entityId) {
        log.debug("Querying blockchain logs by entity: {}={}", entityType, entityId);
        return blockchainLogRepo.findByEntityTypeAndEntityIdOrderByTimestampAsc(entityType, entityId);
    }
    
    @Cacheable(value = "traceability", key = "#foodId", unless = "#result == null")
    @Transactional(readOnly = true)
    public Object getTraceabilityInfo(String foodId) {
        log.debug("Fetching traceability info for: {}", foodId);
        
        if (!cacheService.mightContainFood(foodId)) {
            log.warn("Food {} not found in Bloom Filter", foodId);
            return null;
        }
        
        List<BlockchainLog> logs = blockchainLogRepo.findAllByEntityId(foodId);
        
        return buildTraceabilityInfo(logs);
    }
    
    private Object buildTraceabilityInfo(List<BlockchainLog> logs) {
        return logs.stream()
            .map(log -> TraceabilityRecord.fromEntity(log))
            .toList();
    }
    
    @CacheEvict(value = "blockchain:log", key = "#entityType + ':' + #entityId")
    public void evictBlockchainLogCache(String entityType, Long entityId) {
    }
    
    @CacheEvict(value = "traceability", key = "#foodId")
    public void evictTraceabilityCache(String foodId) {
    }
    
    public record TraceabilityRecord(
        Long id,
        String chainType,
        String entityType,
        Long entityId,
        String action,
        String currentHash,
        String dataHash,
        String offchainRef,
        java.time.LocalDateTime timestamp
    ) {
        public static TraceabilityRecord fromEntity(BlockchainLog log) {
            return new TraceabilityRecord(
                log.getId(),
                log.getChainType(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getCurrentHash(),
                log.getDataHash(),
                log.getOffchainReference(),
                log.getTimestamp()
            );
        }
    }
}
