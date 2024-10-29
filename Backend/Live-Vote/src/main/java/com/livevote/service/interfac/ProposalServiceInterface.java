package com.livevote.service.interfac;

import com.livevote.dto.ProposalDetailsResponse;
import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProposalServiceInterface {
    Response createProposal(ProposalRequest proposalRequest, MultipartFile avatar, List<MultipartFile> choiceAvatars) throws Exception;

    ProposalDetailsResponse viewProposalDetails(String proposalId);

    List<ProposalDetailsResponse> viewAllProposals();
}
