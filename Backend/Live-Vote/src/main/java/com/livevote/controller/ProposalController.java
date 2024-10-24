package com.livevote.controller;

import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import com.livevote.service.interfac.ProposalServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proposals")

public class ProposalController {
    private static final Logger log = LoggerFactory.getLogger(ProposalController.class);
    @Autowired
    private ProposalServiceInterface proposalService;
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/create-proposal")
    public ResponseEntity<Response> createProposal(@RequestBody ProposalRequest proposalRequest) {
        log.info("/create-proposal");
        Response response = proposalService.createProposal(proposalRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
