package com.foodtraceability.agent.impl;

import com.foodtraceability.agent.core.AbstractAgent;
import com.foodtraceability.agent.core.Agent;
import com.foodtraceability.agent.credential.CertificateAuthority;
import com.foodtraceability.agent.credential.AgentCertificate;
import com.foodtraceability.agent.ledger.AgentReputation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CertificateAuthorityAgent extends AbstractAgent {
    
    private static final Logger log = LoggerFactory.getLogger(CertificateAuthorityAgent.class);
    
    private final CertificateAuthority certificateAuthority;
    
    @Autowired
    public CertificateAuthorityAgent(CertificateAuthority certificateAuthority) {
        super("CA-" + System.currentTimeMillis(), AgentType.CERTIFICATE_AUTHORITY);
        this.certificateAuthority = certificateAuthority;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        setState(AgentState.ACTIVE);
        addMetadata("capability", "certificate_authority");
        addMetadata("service_type", "ca");
        addMetadata("is_primary", "true");
        log.info("CA Agent initialized: {}", getAgentId());
    }
    
    public AgentCertificate registerAgent(String agentId, String commonName, long validityDays) {
        if (!isAuthorized()) {
            throw new IllegalStateException("CA Agent not authorized");
        }
        
        log.info("Registering agent: {}, commonName: {}", agentId, commonName);
        AgentCertificate cert = certificateAuthority.issueCertificate(agentId, commonName, validityDays);
        return cert;
    }
    
    public void revokeAgent(String agentId, String reason) {
        if (!isAuthorized()) {
            throw new IllegalStateException("CA Agent not authorized");
        }
        
        log.warn("Revoking agent: {}, reason: {}", agentId, reason);
        certificateAuthority.revokeCertificate(agentId, reason);
        
        long currentScore = getCreditScore();
        if (currentScore < 50) {
            updateCreditScore(-100);
        }
    }
    
    public boolean validateAgent(String agentId) {
        return certificateAuthority.validateCertificate(agentId);
    }
    
    public void updateCreditForValidation(boolean valid) {
        if (valid) {
            updateCreditScore(2);
        } else {
            updateCreditScore(-5);
        }
    }
    
    public CertificateAuthority getCertificateAuthority() {
        return certificateAuthority;
    }
    
    @Override
    public void shutdown() {
        setState(AgentState.SUSPENDED);
        log.info("CA Agent shutdown: {}", getAgentId());
    }
}
