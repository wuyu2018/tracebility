package com.foodtraceability.service;

import com.foodtraceability.entity.BlockchainRetryTask;
import com.foodtraceability.repository.BlockchainRetryTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlockchainRetryService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainRetryService.class);

    static final long MAX_RETRY_DELAY_MINUTES = 60;
    static final int MAX_RETRIES = 5;
    static final long SCHEDULER_FIXED_DELAY_MS = 30_000;

    private final BlockchainRetryTaskRepository retryTaskRepo;
    private final BlockchainService blockchainService;

    public BlockchainRetryService(BlockchainRetryTaskRepository retryTaskRepo,
                                  BlockchainService blockchainService) {
        this.retryTaskRepo = retryTaskRepo;
        this.blockchainService = blockchainService;
    }

    /**
     * 在 AFTER_COMMIT 监听器失败时调度一个重试任务。
     * 所有参数都是已经物化（materialized）的值，重试时不需要重新查库。
     */
    @Transactional
    public void scheduleRetry(String chainType, Long batchId, String entityType, Long entityId,
                              String action, String dataSnapshot, Long operatorId,
                              String eventType, String errorMessage) {
        BlockchainRetryTask task = new BlockchainRetryTask();
        task.setChainType(chainType);
        task.setBatchId(batchId);
        task.setEntityType(entityType);
        task.setEntityId(entityId);
        task.setAction(action);
        task.setDataSnapshot(dataSnapshot);
        task.setOperatorId(operatorId);
        task.setEventType(eventType);
        task.setLastErrorMessage(truncate(errorMessage, 500));
        task.setRetryCount(0);
        task.setMaxRetries(MAX_RETRIES);
        task.setStatus("PENDING");
        task.setNextRetryTime(LocalDateTime.now().plusMinutes(1));

        retryTaskRepo.save(task);
        log.warn("[BlockchainRetry] Retry task scheduled: eventType={}, chainType={}, entityType={}, id={}" +
                        ", batchId={}, error={}",
                eventType, chainType, entityType, entityId, batchId, errorMessage);
    }

    /**
     * 每 30 秒轮询一次待重试任务，指数退避重试。
     */
    @Scheduled(fixedDelay = SCHEDULER_FIXED_DELAY_MS)
    @Transactional
    public void processRetryQueue() {
        List<BlockchainRetryTask> tasks = retryTaskRepo
                .findByStatusAndNextRetryTimeBeforeOrderByCreatedAtAsc(
                        "PENDING", LocalDateTime.now());

        for (BlockchainRetryTask task : tasks) {
            processTask(task);
        }
    }

    private void processTask(BlockchainRetryTask task) {
        task.setStatus("PROCESSING");
        task.setLastRetriedAt(LocalDateTime.now());
        retryTaskRepo.save(task);

        try {
            if ("BATCH".equals(task.getChainType())) {
                blockchainService.appendBatchChainBlock(
                        task.getBatchId(), task.getEntityType(), task.getEntityId(),
                        task.getAction(), task.getDataSnapshot(), task.getOperatorId());
            } else {
                blockchainService.appendMaterialChainBlock(
                        task.getEntityType(), task.getEntityId(),
                        task.getAction(), task.getDataSnapshot(), task.getOperatorId());
            }

            retryTaskRepo.delete(task);
            log.warn("[BlockchainRetry] Retry succeeded: taskId={}, eventType={}, entityType={}, id={}" +
                            ", batchId={}, retryCount={}",
                    task.getId(), task.getEventType(), task.getEntityType(),
                    task.getEntityId(), task.getBatchId(), task.getRetryCount());

        } catch (Exception e) {
            task.setRetryCount(task.getRetryCount() + 1);
            task.setLastErrorMessage(truncate(e.getMessage(), 500));

            if (task.getRetryCount() >= task.getMaxRetries()) {
                task.setStatus("FAILED");
                log.error("[BlockchainRetry] Retry exhausted: taskId={}, eventType={}, entityType={}, id={}" +
                                ", batchId={}, retryCount={}, error={}",
                        task.getId(), task.getEventType(), task.getEntityType(),
                        task.getEntityId(), task.getBatchId(), task.getRetryCount(), e.getMessage());
            } else {
                long delayMinutes = backoffDelay(task.getRetryCount());
                task.setNextRetryTime(LocalDateTime.now().plusMinutes(delayMinutes));
                task.setStatus("PENDING");
                log.warn("[BlockchainRetry] Retry failed, will retry in {} min: taskId={}, eventType={}" +
                                ", entityType={}, id={}, retryCount={}, error={}",
                        delayMinutes, task.getId(), task.getEventType(), task.getEntityType(),
                        task.getEntityId(), task.getRetryCount(), e.getMessage());
            }
            retryTaskRepo.save(task);
        }
    }

    /**
     * 指数退避：2^retryCount 分钟，上限 1 小时。
     */
    static long backoffDelay(int retryCount) {
        return Math.min((long) Math.pow(2, retryCount), MAX_RETRY_DELAY_MINUTES);
    }

    public List<BlockchainRetryTask> getFailedTasks() {
        return retryTaskRepo.findByStatusOrderByCreatedAtAsc("FAILED");
    }

    public long getPendingCount() {
        return retryTaskRepo.countByStatus("PENDING");
    }

    public long getFailedCount() {
        return retryTaskRepo.countByStatus("FAILED");
    }

    private static String truncate(String s, int maxLen) {
        return s != null && s.length() > maxLen ? s.substring(0, maxLen) : s;
    }
}
