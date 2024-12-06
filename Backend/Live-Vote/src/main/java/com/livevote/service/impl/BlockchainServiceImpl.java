
package com.livevote.service.impl;

import com.livevote.blockchain.BlockchainService;
import com.livevote.contracts.VotingRooms;
import com.livevote.entity.VotingProposal;
import com.livevote.repository.VotingProposalRepository;
import com.livevote.service.interfac.BlockchainServiceInterface;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.gas.DefaultGasProvider;

@Service
public class BlockchainServiceImpl implements BlockchainServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainServiceImpl.class);
    private final BlockchainService blockchainService;
    private final VotingProposalRepository proposalRepository;


    @Autowired
    public BlockchainServiceImpl(BlockchainService blockchainService, VotingProposalRepository proposalRepository) {
        this.blockchainService = blockchainService;
        this.proposalRepository = proposalRepository;
    }

    private VotingRooms getAdminVotingRooms() {
        return this.blockchainService.getVotingRooms();
    }

    public String createRoom(String roomId, int tokenLimit, List<String> candidateIds) {
        try {
            int numericRoomId = Integer.parseInt(roomId.substring(4));

            List<BigInteger> candidateBigIntegers = candidateIds.stream()
                    .map(candidateId -> new BigInteger(candidateId.replaceAll("\\D", "")))
                    .collect(Collectors.toList());

            VotingRooms votingRooms = this.getAdminVotingRooms();
            votingRooms.createRoom(
                    BigInteger.valueOf(numericRoomId),
                    BigInteger.valueOf(tokenLimit),
                    candidateBigIntegers
            ).send();

            return "Voting room created successfully on blockchain";
        } catch (Exception e) {
            logger.error("Error creating voting room", e);
            return "Error creating voting room: " + e.getMessage();
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

    public void distributeTokens(String roomId, String userAddress ) throws Exception {
        try {
            int numericRoomId = Integer.parseInt(roomId.substring(4));
            VotingRooms votingRooms = this.getAdminVotingRooms();
            votingRooms.distributeTokens(BigInteger.valueOf((long)numericRoomId), userAddress).send();
        } catch (Exception var6) {
            Exception e = var6;
            logger.error("Error distributing tokens", e);
            throw new Exception("Error distributing tokens: " + e.getMessage());
        }
    }

    @Override
    public BigInteger getUserTokenBalanceInRoom(String roomId, String userAddress) {
        try {
            int numericRoomId = Integer.parseInt(roomId.substring(4));
            VotingRooms votingRooms = this.getAdminVotingRooms();
            return votingRooms.getUserTokenBalanceInRoom(BigInteger.valueOf(numericRoomId), userAddress).send();
        } catch (Exception var4) {
            logger.error("Error getting user token balance", var4);
            throw new RuntimeException("Error getting user token balance: " + var4.getMessage());
        }
    }

    public String getRoomDetails(String roomId) {
        try {
            BigInteger numericRoomId = BigInteger.valueOf((long) Integer.parseInt(roomId.substring(4)));
            VotingRooms votingRooms = this.getAdminVotingRooms();

            Tuple2<BigInteger, List<BigInteger>> roomDetails = (Tuple2) votingRooms.getRoomDetails(numericRoomId).send();

            BigInteger tokenLimit = roomDetails.component1();
            List<BigInteger> candidateIds = roomDetails.component2();

            Tuple2<List<BigInteger>, List<BigInteger>> roomResults = (Tuple2) votingRooms.getRoomResults(numericRoomId).send();
            List<BigInteger> voteCounts = roomResults.component2();

            Tuple2<BigInteger, BigInteger> roomTokenInfo = (Tuple2) votingRooms.rooms(numericRoomId).send();
            BigInteger distributedTokens = roomTokenInfo.component2();

            BigInteger remainingTokens = tokenLimit.subtract(distributedTokens);

            StringBuilder details = new StringBuilder("Room ID: " + roomId + "\n");
            details.append("Token Limit: ").append(tokenLimit).append("\n");
            details.append("Distributed Tokens: ").append(distributedTokens).append("\n");
            details.append("Remaining Tokens: ").append(remainingTokens).append("\n");
            details.append("Candidates:\n");

            for (int i = 0; i < candidateIds.size(); ++i) {
                details.append("Candidate ID: ").append(candidateIds.get(i)).append(", Votes: ").append(voteCounts.get(i)).append("\n");
            }

            return details.toString();
        } catch (Exception e) {
            logger.error("Error getting room details", e);
            throw new RuntimeException("Error getting room details: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getRoomResults(String roomId) {
        try {
            BigInteger numericRoomId = BigInteger.valueOf((long) Integer.parseInt(roomId.substring(4)));

            VotingRooms votingRooms = this.getAdminVotingRooms();

            Tuple2<List<BigInteger>, List<BigInteger>> roomResults = (Tuple2) votingRooms.getRoomResults(numericRoomId).send();

            List<BigInteger> candidateIds = roomResults.component1();
            List<BigInteger> voteCounts = roomResults.component2();

            List<Integer> scores = new ArrayList<>();
            for (BigInteger voteCount : voteCounts) {
                scores.add(voteCount.intValue());
            }

            int scoresTotal = scores.stream().mapToInt(Integer::intValue).sum();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("proposalId", roomId);
            result.put("scores", scores);
            result.put("scoresTotal", scoresTotal);

            return result;
        } catch (Exception e) {
            logger.error("Error getting room results", e);
            throw new RuntimeException("Error getting room results: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getClosedRoomsDetailsFromProposals() {
        try {
            List<VotingProposal> closedProposals = proposalRepository.findByState("closed");

            List<BigInteger> roomIds = new ArrayList<>();
            for (VotingProposal proposal : closedProposals) {
                String proposalId = proposal.getProposalId().replace("PRP_", "");
                roomIds.add(new BigInteger(proposalId));
            }

            BigInteger[] roomIdsArray = roomIds.toArray(new BigInteger[0]);

            VotingRooms votingRooms = this.getAdminVotingRooms();

            List<BigInteger> numericRoomIds = Arrays.asList(roomIdsArray);

            Tuple2<List<BigInteger>, List<List<org.web3j.abi.datatypes.generated.Uint256>>> closedRoomDetails =
                    (Tuple2) votingRooms.getClosedRoomsDetails(numericRoomIds).send();

            List<BigInteger> selectedRoomIds = closedRoomDetails.component1();
            List<List<org.web3j.abi.datatypes.generated.Uint256>> candidateVotesList = closedRoomDetails.component2();

            List<Map<String, Object>> result = new ArrayList<>();

            for (BigInteger roomId : selectedRoomIds) {
                Map<String, Object> roomDetails = new LinkedHashMap<>();
                roomDetails.put("proposalId", "PRP_" + String.format("%05d", roomId)); // Format as PRP_00012

                List<Long> scores = new ArrayList<>();
                long totalScore = 0;

                int roomIndex = selectedRoomIds.indexOf(roomId);
                List<org.web3j.abi.datatypes.generated.Uint256> votesForRoom = candidateVotesList.get(roomIndex);

                for (org.web3j.abi.datatypes.generated.Uint256 vote : votesForRoom) {
                    long voteCount = vote.getValue().longValue();
                    scores.add(voteCount);
                    totalScore += voteCount;
                }

                roomDetails.put("scores", scores);

                roomDetails.put("scoresTotal", totalScore);

                result.add(roomDetails);
            }

            return result;
        } catch (Exception e) {
            logger.error("Error getting closed room details", e);
            throw new RuntimeException("Error getting closed room details: " + e.getMessage());
        }
    }
}
