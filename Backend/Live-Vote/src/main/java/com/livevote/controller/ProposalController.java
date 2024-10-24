package com.livevote.controller;

import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import com.livevote.service.interfac.ProposalServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")

public class ProposalController {
    private static final Logger log = LoggerFactory.getLogger(ProposalController.class);
    @Autowired
    private ProposalServiceInterface proposalService;
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/create-proposal")
    public ResponseEntity<Response> createProposal(
            @RequestPart("proposalData") ProposalRequest proposalRequest,
            @RequestPart("files") List<MultipartFile> files
    ) throws Exception {
        MultipartFile avatar = files.get(0);
        List<MultipartFile> choiceAvatars = files.subList(1, files.size());

        Response response = proposalService.createProposal(proposalRequest, avatar, choiceAvatars);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


}
