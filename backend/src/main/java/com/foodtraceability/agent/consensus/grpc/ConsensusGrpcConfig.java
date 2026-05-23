package com.foodtraceability.agent.consensus.grpc;

import com.foodtraceability.agent.consensus.transport.ConsensusTransport;
import com.foodtraceability.agent.consensus.transport.GrpcConsensusTransport;
import com.foodtraceability.agent.consensus.transport.InProcessConsensusTransport;
import com.foodtraceability.agent.contract.DataOnChainContract;
import com.foodtraceability.agent.contract.PermissionControlContract;
import com.foodtraceability.agent.core.MultiAgentCoordinator;
import com.foodtraceability.service.BlockchainService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Configuration
public class ConsensusGrpcConfig {

    private static final Logger log = LoggerFactory.getLogger(ConsensusGrpcConfig.class);

    private final ConsensusPeerConfig peerConfig;
    private final MultiAgentCoordinator agentCoordinator;
    private final PermissionControlContract permissionControlContract;
    private final DataOnChainContract dataOnChainContract;
    private final ConsensusServiceGrpcImpl consensusGrpcService;
    private final BlockchainService blockchainService;

    public ConsensusGrpcConfig(ConsensusPeerConfig peerConfig,
                                MultiAgentCoordinator agentCoordinator,
                                PermissionControlContract permissionControlContract,
                                DataOnChainContract dataOnChainContract,
                                ConsensusServiceGrpcImpl consensusGrpcService,
                                BlockchainService blockchainService) {
        this.peerConfig = peerConfig;
        this.agentCoordinator = agentCoordinator;
        this.permissionControlContract = permissionControlContract;
        this.dataOnChainContract = dataOnChainContract;
        this.consensusGrpcService = consensusGrpcService;
        this.blockchainService = blockchainService;
    }

    private Server grpcServer;

    @Bean
    public ConsensusTransport consensusTransport() {
        if (peerConfig.isGrpcEnabled()) {
            return new GrpcConsensusTransport(
                    peerConfig, permissionControlContract, dataOnChainContract,
                    null, null, null);
        }
        return new InProcessConsensusTransport(
                new ArrayList<>(agentCoordinator.getAllAgents()),
                permissionControlContract, dataOnChainContract,
                blockchainService);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (peerConfig.isGrpcServerStart()) {
            try {
                grpcServer = ServerBuilder.forPort(peerConfig.getGrpcServerPort())
                        .addService(consensusGrpcService)
                        .build()
                        .start();
                log.info("gRPC server started on port {} (agent: {}, role: {})",
                        peerConfig.getGrpcServerPort(), peerConfig.getAgentId(), peerConfig.getAgentRole());

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (grpcServer != null) {
                        grpcServer.shutdown();
                        try {
                            grpcServer.awaitTermination(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }));
            } catch (IOException e) {
                log.error("Failed to start gRPC server", e);
            }
        }
    }
}
