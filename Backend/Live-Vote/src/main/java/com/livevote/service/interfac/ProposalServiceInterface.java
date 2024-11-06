//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.livevote.service.interfac;

import com.livevote.dto.ProposalDetailsResponse;
import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ProposalServiceInterface {
    Response createProposal(ProposalRequest proposalRequest, MultipartFile avatar, List<MultipartFile> choiceAvatars) throws Exception;

    ProposalDetailsResponse viewProposalDetails(String proposalId);

    List<ProposalDetailsResponse> viewAllProposals();
}
