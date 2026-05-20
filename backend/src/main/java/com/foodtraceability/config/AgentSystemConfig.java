package com.foodtraceability.config;

import com.foodtraceability.agent.core.MultiAgentCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class AgentSystemConfig {
    
    private static final Logger log = LoggerFactory.getLogger(AgentSystemConfig.class);
    
    private final MultiAgentCoordinator agentCoordinator;
    
    @Autowired
    public AgentSystemConfig(MultiAgentCoordinator agentCoordinator) {
        this.agentCoordinator = agentCoordinator;
    }
    
    @PostConstruct
    public void startAgentSystem() {
        log.info("Starting Agent System...");
        agentCoordinator.initialize();
        log.info("Agent System started successfully");
    }
    
    @PreDestroy
    public void stopAgentSystem() {
        log.info("Stopping Agent System...");
        agentCoordinator.shutdown();
        log.info("Agent System stopped");
    }
}
