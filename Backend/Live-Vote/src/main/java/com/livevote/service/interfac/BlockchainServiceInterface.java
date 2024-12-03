
package com.livevote.service.interfac;

import java.math.BigInteger;
import java.util.List;

public interface BlockchainServiceInterface {
    String createRoom(String roomId, int tokenLimit, List<String> candidateIds);

    String vote(String roomId, int candidateId, String userPrivateKey);

    String getCandidateVotes(String roomId, int candidateId);

    BigInteger getRoomCount();

    String getRoomDetails(String roomId);

    void distributeTokens(String roomId, String userAddress, int amount) throws Exception;

    BigInteger getUserTokenBalanceInRoom(String roomId, String userAddress);

}
