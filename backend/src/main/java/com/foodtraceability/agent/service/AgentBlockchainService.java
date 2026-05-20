package com.foodtraceability.agent.service;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.consensus.PbftConsensus;
import com.foodtraceability.entity.BlockchainLog;
import com.foodtraceability.service.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentBlockchainService {
    
    private static final Logger log = LoggerFactory.getLogger(AgentBlockchainService.class);
    
    private final MultiAgentCoordinator agentCoordinator;
    private final BlockchainService blockchainService;
    private final DataOnChainContract dataOnChainContract;
    
    public AgentBlockchainService(
            MultiAgentCoordinator agentCoordinator,
            BlockchainService blockchainService,
            DataOnChainContract dataOnChainContract) {
        this.agentCoordinator = agentCoordinator;
        this.blockchainService = blockchainService;
        this.dataOnChainContract = dataOnChainContract;
    }
    
    @Transactional
    public BlockchainLog appendBlockWithConsensus(
            String chainType,
            String entityType,
            Long entityId,
            String action,
            String dataSnapshot,
            Long operatorId) {
        
        log.info("Appending block with PBFT consensus: type={}, entity={}, id={}", 
                chainType, entityType, entityId);
        
        var currentAgent = getCurrentAgentForChainType(chainType);
        
        if (!currentAgent.isAuthorized()) {
            throw new IllegalStateException("Agent not authorized for blockchain operation");
        }
        
        String context = entityType + "|" + entityId + "|" + action + "|" + calculateDataHash(dataSnapshot);
        
        if (!dataOnChainContract.validate(context)) {
            throw new IllegalStateException("Data on-chain validation failed");
        }
        
        PbftConsensus pbft = agentCoordinator.getPbftConsensus();
        PbftConsensus.PbftMessage request = pbft.createRequest(context);
        
        log.debug("Created PBFT request: seq={}", request.getSequenceNumber());
        
        BlockchainLog block;
        if ("MATERIAL".equals(chainType)) {
            block = blockchainService.appendMaterialChainBlock(
                    entityType, entityId, action, dataSnapshot, operatorId);
        } else if ("BATCH".equals(chainType)) {
            block = blockchainService.appendBatchChainBlock(
                    null, entityType, entityId, action, dataSnapshot, operatorId);
        } else {
            throw new IllegalArgumentException("Unknown chain type: " + chainType);
        }
        
        currentAgent.updateCreditScore(1);
        
        log.info("Block appended successfully: hash={}", block.getCurrentHash());
        
        return block;
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
    
    private String calculateDataHash(String dataSnapshot) {
        if (dataSnapshot == null) {
            return "";
        }
        return Integer.toHexString(dataSnapshot.hashCode());
    }
}
