package com.foodtraceability.agent.consensus.transport;

import com.foodtraceability.agent.consensus.grpc.ConsensusPeerConfig;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.contract.PermissionControlContract;
import com.foodtraceability.agent.core.MultiAgentCoordinator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsensusTransportConfig {

    private final ConsensusPeerConfig peerConfig;
    private final PermissionControlContract permissionControlContract;
    private final DataOnChainContract dataOnChainContract;

    public ConsensusTransportConfig(ConsensusPeerConfig peerConfig,
                                     PermissionControlContract permissionControlContract,
                                     DataOnChainContract dataOnChainContract) {
        this.peerConfig = peerConfig;
        this.permissionControlContract = permissionControlContract;
        this.dataOnChainContract = dataOnChainContract;
    }

    @Bean
    public ConsensusTransport consensusTransport() {
        return new GrpcConsensusTransport(
                peerConfig, permissionControlContract, dataOnChainContract,
                null, null, null);
    }
}
