
package com.livevote.service.impl;

import com.livevote.blockchain.BlockchainService;
import com.livevote.contracts.VotingRooms;
import com.livevote.service.interfac.BlockchainServiceInterface;
import java.math.BigInteger;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.DefaultGasProvider;

@Service
public class BlockchainServiceImpl implements BlockchainServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainServiceImpl.class);
    private final BlockchainService blockchainService;

    @Autowired
    public BlockchainServiceImpl(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    private VotingRooms getAdminVotingRooms() {
        return this.blockchainService.getVotingRooms();
    }

    private VotingRooms getUserVotingRooms(String userPrivateKey) {
        Credentials userCredentials = Credentials.create(userPrivateKey);
        return VotingRooms.load("0x5FbDB2315678afecb367f032d93F642f64180aa3", this.blockchainService.getWeb3j(), userCredentials, new DefaultGasProvider());
    }

    public String createRoom(String roomId, int tokenLimit, List<Integer> candidateIds) {
        try {
            int numericRoomId = Integer.parseInt(roomId.substring(4));
            VotingRooms votingRooms = this.getAdminVotingRooms();
            votingRooms.createRoom(BigInteger.valueOf((long)numericRoomId), BigInteger.valueOf((long)tokenLimit), List.of((BigInteger[])candidateIds.stream().map(BigInteger::valueOf).toArray((x$0) -> {
                return new BigInteger[x$0];
            }))).send();
            return "Voting room created successfully on blockchain";
        } catch (Exception var6) {
            Exception e = var6;
            logger.error("Error creating voting room", e);
            return "Error creating voting room: " + e.getMessage();
        }
    }

    public String vote(String roomId, int candidateId, String userPrivateKey) {
        try {
            int numericRoomId = Integer.parseInt(roomId.substring(4));
            VotingRooms votingRoomsForUser = this.getUserVotingRooms(userPrivateKey);
            votingRoomsForUser.vote(BigInteger.valueOf((long)numericRoomId), BigInteger.valueOf((long)candidateId)).send();
            return "Vote cast successfully";
        } catch (Exception var6) {
            Exception e = var6;
            logger.error("Error voting", e);
            return "Error voting: " + e.getMessage();
        }
    }

    public String getCandidateVotes(String roomId, int candidateId) {
        try {
            int numericRoomId = Integer.parseInt(roomId.substring(4));
            VotingRooms votingRooms = this.getAdminVotingRooms();
            BigInteger votes = (BigInteger)votingRooms.getCandidateVotes(BigInteger.valueOf((long)numericRoomId), BigInteger.valueOf((long)candidateId)).send();
            return "Candidate " + candidateId + " has " + String.valueOf(votes) + " votes";
        } catch (Exception var6) {
            Exception e = var6;
            logger.error("Error getting candidate votes", e);
            return "Error getting votes: " + e.getMessage();
        }
    }

    public BigInteger getRoomCount() {
        try {
            VotingRooms votingRooms = this.getAdminVotingRooms();
            return (BigInteger)votingRooms.roomCounter().send();
        } catch (Exception var2) {
            Exception e = var2;
            logger.error("Error getting room count", e);
            throw new RuntimeException("Error getting room count: " + e.getMessage());
        }
    }

    public String getRoomDetails(String roomId) {
        try {
            BigInteger numericRoomId = BigInteger.valueOf((long)Integer.parseInt(roomId.substring(4)));
            VotingRooms votingRooms = this.getAdminVotingRooms();
            Tuple3<BigInteger, List<BigInteger>, Boolean> roomDetails = (Tuple3)votingRooms.getRoomDetails(numericRoomId).send();
            BigInteger tokenLimit = (BigInteger)roomDetails.component1();
            List<BigInteger> candidateIds = (List)roomDetails.component2();
            Boolean votingOpen = (Boolean)roomDetails.component3();
            List<BigInteger> voteCounts = (List)((Tuple2)votingRooms.getRoomResults(numericRoomId).send()).component2();
            BigInteger distributedTokens = (BigInteger)((Tuple3)votingRooms.rooms(numericRoomId).send()).component2();
            BigInteger remainingTokens = tokenLimit.subtract(distributedTokens);
            StringBuilder details = new StringBuilder("Room ID: " + roomId + "\n");
            details.append("Token Limit: ").append(tokenLimit).append("\n");
            details.append("Distributed Tokens: ").append(distributedTokens).append("\n");
            details.append("Remaining Tokens: ").append(remainingTokens).append("\n");
            details.append("Voting Status: ").append(votingOpen ? "Open" : "Closed").append("\n");
            details.append("Candidates:\n");

            for(int i = 0; i < candidateIds.size(); ++i) {
                details.append("Candidate ID: ").append(candidateIds.get(i)).append(", Votes: ").append(voteCounts.get(i)).append("\n");
            }

            return details.toString();
        } catch (Exception var13) {
            Exception e = var13;
            logger.error("Error getting room details", e);
            throw new RuntimeException("Error getting room details: " + e.getMessage());
        }
    }

    public void distributeTokens(String roomId, String userAddress, int amount) throws Exception {
        try {
            int numericRoomId = Integer.parseInt(roomId.substring(4));
            VotingRooms votingRooms = this.getAdminVotingRooms();
            votingRooms.distributeTokens(BigInteger.valueOf((long)numericRoomId), userAddress, BigInteger.valueOf((long)amount)).send();
        } catch (Exception var6) {
            Exception e = var6;
            logger.error("Error distributing tokens", e);
            throw new Exception("Error distributing tokens: " + e.getMessage());
        }
    }
}