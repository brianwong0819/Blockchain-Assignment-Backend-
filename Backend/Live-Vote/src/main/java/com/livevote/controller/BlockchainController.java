package com.livevote.controller;

import com.livevote.service.interfac.BlockchainServiceInterface;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/voting"})
public class BlockchainController {
    private final BlockchainServiceInterface votingService;

    @Autowired
    public BlockchainController(BlockchainServiceInterface votingService) {
        this.votingService = votingService;
    }

    @PostMapping({"/vote"})
    public ResponseEntity<String> vote(@RequestParam String roomId, @RequestParam int candidateId, @RequestParam String userPrivateKey) {
        try {
            String result = this.votingService.vote(roomId, candidateId, userPrivateKey);
            return ResponseEntity.ok(result);
        } catch (Exception var5) {
            Exception e = var5;
            return ResponseEntity.status(500).body("Failed to cast vote: " + e.getMessage());
        }
    }

    @GetMapping({"/getVotes"})
    public ResponseEntity<String> getCandidateVotes(@RequestParam String roomId, @RequestParam int candidateId) {
        try {
            String result = this.votingService.getCandidateVotes(roomId, candidateId);
            return ResponseEntity.ok(result);
        } catch (Exception var4) {
            Exception e = var4;
            return ResponseEntity.status(500).body("Failed to get candidate votes: " + e.getMessage());
        }
    }

    @GetMapping({"/getRoomCount"})
    public ResponseEntity<BigInteger> getRoomCount() {
        try {
            BigInteger count = this.votingService.getRoomCount();
            return ResponseEntity.ok(count);
        } catch (Exception var2) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping({"/getRoomDetails"})
    public ResponseEntity<String> getRoomDetails(@RequestParam String roomId) {
        try {
            String details = this.votingService.getRoomDetails(roomId);
            return ResponseEntity.ok(details);
        } catch (Exception var3) {
            Exception e = var3;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping({"/distributeTokens"})
    public ResponseEntity<String> distributeTokens(@RequestParam String roomId, @RequestParam String userAddress, @RequestParam int amount) {
        try {
            this.votingService.distributeTokens(roomId, userAddress, amount);
            return ResponseEntity.ok("Tokens distributed successfully");
        } catch (Exception var5) {
            Exception e = var5;
            return ResponseEntity.status(500).body("Failed to distribute tokens: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping({"/getUserTokenBalance"})
    public ResponseEntity<Map<String, BigInteger>> getUserTokenBalanceInRoom(@RequestParam String roomId, @RequestParam String userAddress) {
        try {
            BigInteger balance = this.votingService.getUserTokenBalanceInRoom(roomId, userAddress);
            System.out.println("User token balance: " + balance);
            Map<String, BigInteger> response = new HashMap<>();
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception var4) {
            Map<String, BigInteger> errorResponse = new HashMap<>();
            errorResponse.put("balance", BigInteger.ZERO);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
