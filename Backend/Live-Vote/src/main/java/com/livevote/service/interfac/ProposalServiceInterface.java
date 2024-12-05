//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.livevote.service.interfac;

import com.livevote.dto.ProposalDetailsResponse;
import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import java.util.List;

import com.livevote.dto.VotingResultResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProposalServiceInterface {
    Response createProposal(ProposalRequest proposalRequest, MultipartFile avatar, List<MultipartFile> choiceAvatars) throws Exception;

    ProposalDetailsResponse viewProposalDetails(String proposalId);

    List<ProposalDetailsResponse> viewAllProposals();

    List<String> getTokenQr(String proposalId);

    Response updateQrStatus (String proposalId, String qrCode, String userWalletAddress);

    Response saveVotingResult(String proposalId, String userWalletAddress, String choiceId);

    List<String> getUserVotedProposals(String userWalletAddress);

    Response validateQrStatus(String qrCode);

    List<VotingResultResponse> getVotingResult(String proposalId, String userWalletAddress);
}
