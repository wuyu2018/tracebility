package com.foodtraceability.security;

import com.foodtraceability.agent.core.Agent;
import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {
    
    private static final Logger log = LoggerFactory.getLogger(AccessControlService.class);
    
    private final MultiAgentCoordinator agentCoordinator;
    private final ObjectMapper objectMapper;
    
    public AccessControlService(MultiAgentCoordinator agentCoordinator) {
        this.agentCoordinator = agentCoordinator;
        this.objectMapper = new ObjectMapper();
    }
    
    public boolean hasPermission(String foodId, String action, String agentId) {
        try {
            Agent agent = agentCoordinator.getAgent(agentId);
            if (agent == null) {
                log.warn("Agent not found: {}", agentId);
                return false;
            }
            
            if (!agent.isAuthorized()) {
                log.warn("Agent not authorized: {}", agentId);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Access control check failed", e);
            return false;
        }
    }
}
