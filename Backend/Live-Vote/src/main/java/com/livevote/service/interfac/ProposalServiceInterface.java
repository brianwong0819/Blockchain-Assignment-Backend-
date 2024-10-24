package com.livevote.service.interfac;

import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;

public interface ProposalServiceInterface {
    Response createProposal(ProposalRequest proposalRequest);
}
