package com.livevote.blockchain;

import com.livevote.contracts.VotingRooms;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

@Service
public class BlockchainService {
    public static final String NODE_URL = "http://127.0.0.1:8545";
    public static final String CONTRACT_ADDRESS = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
    private Web3j web3j = Web3j.build(new HttpService(NODE_URL));
    private Credentials credentials;
    private VotingRooms votingRooms;

    public BlockchainService() {
        String privateKey = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";

        if (privateKey != null && !privateKey.isEmpty()) {
            this.credentials = Credentials.create(privateKey);
            this.votingRooms = VotingRooms.load(CONTRACT_ADDRESS, this.web3j, this.credentials, new DefaultGasProvider());
        } else {
            throw new IllegalArgumentException("Admin private key cannot be null or empty. Please set it correctly in the environment variables.");
        }
    }

    public VotingRooms getVotingRooms() {
        return this.votingRooms;
    }

    public Web3j getWeb3j() {
        return this.web3j;
    }
}
