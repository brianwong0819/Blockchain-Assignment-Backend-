package com.livevote.controller;

import com.livevote.dto.ProposalDetailsResponse;
import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import com.livevote.dto.VotingResultResponse;
import com.livevote.repository.UserRepository;
import com.livevote.service.interfac.ProposalServiceInterface;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/proposals"})
public class ProposalController {
    private static final Logger log = LoggerFactory.getLogger(ProposalController.class);
    @Autowired
    private ProposalServiceInterface proposalService;
    @Autowired
    private UserRepository userRepository;

    public ProposalController() {
    }

    @CrossOrigin(
            origins = {"http://localhost:5173"}
    )
    @PostMapping({"/create-proposal"})
    public ResponseEntity<Response> createProposal(@RequestPart("proposalData") ProposalRequest proposalRequest, @RequestPart("files") List<MultipartFile> files) throws Exception {
        log.info("create-proposal");
        MultipartFile avatar = (MultipartFile)files.get(0);
        List<MultipartFile> choiceAvatars = files.subList(1, files.size());
        Response response = this.proposalService.createProposal(proposalRequest, avatar, choiceAvatars);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping({"/view-proposal-details/{proposalId}"})
    public ResponseEntity<ProposalDetailsResponse> viewDetails(@PathVariable String proposalId) {
        log.info("view-proposal-details");
        ProposalDetailsResponse response = this.proposalService.viewProposalDetails(proposalId);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(
            origins = {"http://localhost:5173"}
    )
    @GetMapping({"/view-all-proposals"})
    public ResponseEntity<List<ProposalDetailsResponse>> viewAllProposals() {
        log.info("view-all-proposals");
        List<ProposalDetailsResponse> response = this.proposalService.viewAllProposals();
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(
            origins = {"http://localhost:5173"}
    )
    @GetMapping({"/get-token-qr"})
    public ResponseEntity<List<String>> getTokenQr(@RequestParam String proposalId) {
        log.info("get-token-qr");
        List<String> response = this.proposalService.getTokenQr(proposalId);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(
            origins = {"http://localhost:5173"}
    )
    @PostMapping({"/validate-qr-status"})
    public ResponseEntity<Boolean> validateQrStatus(@RequestParam String qrCode) {
        log.info("validate-qr-status");
        Boolean response = this.proposalService.validateQrStatus(qrCode);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(
            origins = {"http://localhost:5173"}
    )
    @PostMapping({"/update-qr-status"})
    public ResponseEntity<Response> updateQrStatus (@RequestParam String proposalId, @RequestParam String qrCode, @RequestParam String userWalletAddress) {
        log.info("update-qr-status");
        Response response = this.proposalService.updateQrStatus(proposalId, qrCode, userWalletAddress);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(
            origins = {"http://localhost:5173"}
    )
    @PostMapping({"/save-voting-result"})
    public ResponseEntity<Response> saveVotingResult(@RequestParam String proposalId, @RequestParam String userWalletAddress, @RequestParam String choiceId) {
        log.info("save-voting-result");
        Response response = this.proposalService.saveVotingResult(proposalId, userWalletAddress, choiceId);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(
            origins = {"http://localhost:5173"}
    )
    @GetMapping({"/get-user-voted-proposal"})
    public ResponseEntity<List<String>> getUserVotedProposal(@RequestParam String userWalletAddress) {
        log.info("get-user-voted-proposal");
        List<String> response = this.proposalService.getUserVotedProposals (userWalletAddress);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(
            origins = {"http://localhost:5173"}
    )
    @PostMapping({"get-voting-result"})
    public ResponseEntity<List<VotingResultResponse>> getVotingResult(@RequestParam String proposalId, @RequestParam(required = false) String userWalletAddress) {
        log.info("get-voting-result");
        List<VotingResultResponse> response = this.proposalService.getVotingResult(proposalId, userWalletAddress);
        return ResponseEntity.ok(response);
    }
}

