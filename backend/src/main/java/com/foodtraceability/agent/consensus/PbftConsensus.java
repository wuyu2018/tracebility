package com.foodtraceability.agent.consensus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PbftConsensus {
    
    private static final Logger log = LoggerFactory.getLogger(PbftConsensus.class);
    
    private final String nodeId;
    private final AtomicLong sequenceNumber;
    private final String view;
    private volatile boolean isPrimary;
    
    private final Map<Long, ConsensusState> consensusStates;
    private final List<String> replicaNodeIds;
    
    private static final int MAX_FAULTY_NODES = 1;
    
    public PbftConsensus() {
        this.nodeId = "node-" + UUID.randomUUID().toString().substring(0, 8);
        this.sequenceNumber = new AtomicLong(0);
        this.view = "0";
        this.isPrimary = false;
        this.consensusStates = new ConcurrentHashMap<>();
        this.replicaNodeIds = new ArrayList<>();
    }
    
    public void initialize(boolean asPrimary, List<String> replicaIds) {
        this.isPrimary = asPrimary;
        this.replicaNodeIds.clear();
        this.replicaNodeIds.addAll(replicaIds);
        log.info("PBFT consensus initialized. Node: {}, Primary: {}, Replicas: {}", 
                nodeId, isPrimary, replicaNodeIds);
    }
    
    public PbftMessage createRequest(String clientRequest) {
        long seqNum = sequenceNumber.incrementAndGet();
        String digest = calculateDigest(clientRequest);
        
        PbftMessage request = new PbftMessage(
            PbftMessage.MessageType.REQUEST,
            view,
            seqNum,
            digest,
            nodeId
        );
        
        log.debug("Created REQUEST message: seq={}, digest={}", seqNum, digest);
        return request;
    }
    
    public PbftMessage createPrePrepare(PbftMessage request) {
        if (!isPrimary) {
            throw new IllegalStateException("Only primary can create PRE-PREPARE message");
        }
        
        PbftMessage prePrepare = new PbftMessage(
            PbftMessage.MessageType.PRE_PREPARE,
            view,
            request.getSequenceNumber(),
            request.getDigest(),
            nodeId
        );
        
        log.debug("Created PRE-PREPARE message: seq={}", request.getSequenceNumber());
        return prePrepare;
    }
    
    public PbftMessage createPrepare(PbftMessage prePrepare) {
        PbftMessage prepare = new PbftMessage(
            PbftMessage.MessageType.PREPARE,
            view,
            prePrepare.getSequenceNumber(),
            prePrepare.getDigest(),
            nodeId
        );
        
        log.debug("Created PREPARE message: seq={}", prePrepare.getSequenceNumber());
        return prepare;
    }
    
    public PbftMessage createCommit(PbftMessage prepare) {
        PbftMessage commit = new PbftMessage(
            PbftMessage.MessageType.COMMIT,
            view,
            prepare.getSequenceNumber(),
            prepare.getDigest(),
            nodeId
        );
        
        log.debug("Created COMMIT message: seq={}", prepare.getSequenceNumber());
        return commit;
    }
    
    public ConsensusResult receivePrePrepare(PbftMessage prePrepare) {
        ConsensusState state = getOrCreateState(prePrepare.getSequenceNumber());
        state.addPrePrepare(prePrepare);
        
        if (validatePrePrepare(prePrepare)) {
            state.setPrePrepareAccepted(true);
            return ConsensusResult.ACCEPTED;
        } else {
            return ConsensusResult.REJECTED;
        }
    }
    
    public int receivePrepare(PbftMessage prepare) {
        ConsensusState state = getOrCreateState(prepare.getSequenceNumber());
        state.addPrepare(prepare);
        
        int prepareCount = state.getPrepareCount();
        log.debug("Received PREPARE from {}. Total prepares: {}", 
                 prepare.getSenderId(), prepareCount);
        
        if (prepareCount >= 2 * MAX_FAULTY_NODES + 1) {
            return prepareCount;
        }
        return prepareCount;
    }
    
    public int receiveCommit(PbftMessage commit) {
        ConsensusState state = getOrCreateState(commit.getSequenceNumber());
        state.addCommit(commit);
        
        int commitCount = state.getCommitCount();
        log.debug("Received COMMIT from {}. Total commits: {}", 
                 commit.getSenderId(), commitCount);
        
        return commitCount;
    }
    
    public boolean canExecute(Long sequenceNumber) {
        ConsensusState state = consensusStates.get(sequenceNumber);
        if (state == null) {
            return false;
        }
        
        int faultyNodes = MAX_FAULTY_NODES;
        int requiredCommits = 2 * faultyNodes + 1;
        
        return state.isPrepared() && state.getCommitCount() >= requiredCommits;
    }
    
    public void markExecuted(Long sequenceNumber) {
        ConsensusState state = consensusStates.get(sequenceNumber);
        if (state != null) {
            state.setExecuted(true);
            log.info("Consensus reached for sequence: {}", sequenceNumber);
        }
    }
    
    private ConsensusState getOrCreateState(Long sequenceNumber) {
        return consensusStates.computeIfAbsent(sequenceNumber, k -> new ConsensusState());
    }
    
    private boolean validatePrePrepare(PbftMessage prePrepare) {
        return prePrepare.getType() == PbftMessage.MessageType.PRE_PREPARE &&
               prePrepare.getView().equals(view) &&
               prePrepare.getDigest() != null;
    }
    
    private String calculateDigest(String data) {
        return Integer.toHexString(data.hashCode());
    }
    
    public enum ConsensusResult {
        ACCEPTED,
        REJECTED,
        PENDING
    }
    
    private static class ConsensusState {
        private PbftMessage prePrepare;
        private final Map<String, PbftMessage> prepares;
        private final Map<String, PbftMessage> commits;
        private volatile boolean prePrepareAccepted;
        private volatile boolean executed;
        
        private ConsensusState() {
            this.prepares = new ConcurrentHashMap<>();
            this.commits = new ConcurrentHashMap<>();
            this.prePrepareAccepted = false;
            this.executed = false;
        }
        
        public void addPrePrepare(PbftMessage message) {
            this.prePrepare = message;
        }
        
        public void addPrepare(PbftMessage message) {
            this.prepares.put(message.getSenderId(), message);
        }
        
        public void addCommit(PbftMessage message) {
            this.commits.put(message.getSenderId(), message);
        }
        
        public int getPrepareCount() {
            return prepares.size() + (prePrepare != null ? 1 : 0);
        }
        
        public int getCommitCount() {
            return commits.size();
        }
        
        public boolean isPrepared() {
            return prePrepareAccepted && getPrepareCount() >= 2 * MAX_FAULTY_NODES + 1;
        }
        
        public boolean isPrePrepareAccepted() {
            return prePrepareAccepted;
        }
        
        public void setPrePrepareAccepted(boolean accepted) {
            this.prePrepareAccepted = accepted;
        }
        
        public boolean isExecuted() {
            return executed;
        }
        
        public void setExecuted(boolean executed) {
            this.executed = executed;
        }
    }
}
