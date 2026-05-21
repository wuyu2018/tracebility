package com.foodtraceability.repository;

import com.foodtraceability.entity.AgentIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentIdentityRepository extends JpaRepository<AgentIdentity, Long> {

    Optional<AgentIdentity> findByAgentId(String agentId);

    Optional<AgentIdentity> findByAgentIdAndStatus(String agentId, String status);
}
