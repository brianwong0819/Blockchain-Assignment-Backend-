
package com.livevote.service.interfac;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface BlockchainServiceInterface {
    String createRoom(String roomId, int tokenLimit, List<String> candidateIds);

    String getCandidateVotes(String roomId, int candidateId);

    BigInteger getRoomCount();

    String getRoomDetails(String roomId);

    void distributeTokens(String roomId, String userAddress) throws Exception;

    BigInteger getUserTokenBalanceInRoom(String roomId, String userAddress);

    Map<String, Object> getRoomResults(String roomId);

    List<Map<String, Object>> getClosedRoomsDetailsFromProposals();
}
