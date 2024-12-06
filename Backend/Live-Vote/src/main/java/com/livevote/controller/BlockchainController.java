package com.livevote.controller;

import com.livevote.service.interfac.BlockchainServiceInterface;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    @CrossOrigin(origins = "http://localhost:5173")
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

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping({"/get-votes"})
    public ResponseEntity<String> getCandidateVotes(@RequestParam String roomId, @RequestParam int candidateId) {
        try {
            String result = this.votingService.getCandidateVotes(roomId, candidateId);
            return ResponseEntity.ok(result);
        } catch (Exception var4) {
            Exception e = var4;
            return ResponseEntity.status(500).body("Failed to get candidate votes: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping({"/get-room-count"})
    public ResponseEntity<BigInteger> getRoomCount() {
        try {
            BigInteger count = this.votingService.getRoomCount();
            return ResponseEntity.ok(count);
        } catch (Exception var2) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping({"/get-room-details"})
    public ResponseEntity<String> getRoomDetails(@RequestParam String roomId) {
        try {
            String details = this.votingService.getRoomDetails(roomId);
            return ResponseEntity.ok(details);
        } catch (Exception var3) {
            Exception e = var3;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping({"/distribute-tokens"})
    public ResponseEntity<String> distributeTokens(@RequestParam String roomId, @RequestParam String userAddress) {
        try {
            this.votingService.distributeTokens(roomId, userAddress);
            return ResponseEntity.ok("Tokens distributed successfully");
        } catch (Exception var5) {
            Exception e = var5;
            return ResponseEntity.status(500).body("Failed to distribute tokens: " + e.getMessage());
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping({"/get-user-token-balance"})
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

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping({"/get-room-results"})
    public ResponseEntity<Map<String, Object>> getRoomResults(@RequestParam String roomId) {
        try {
            Map<String, Object> results = this.votingService.getRoomResults(roomId);

            return ResponseEntity.ok(results);
        } catch (Exception var3) {
            Exception e = var3;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to get room results: " + e.getMessage()));
        }
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/get-closed-room-details")
    public ResponseEntity<List<Map<String, Object>>> getClosedRoomDetailsFromProposals() {
        try {
            List<Map<String, Object>> roomDetails = this.votingService.getClosedRoomsDetailsFromProposals();

            return ResponseEntity.ok(roomDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Error fetching closed room details: " + e.getMessage())));
        }
    }
}
