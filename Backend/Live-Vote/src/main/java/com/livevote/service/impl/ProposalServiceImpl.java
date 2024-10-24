package com.livevote.service.impl;

import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import com.livevote.entity.VotingProposal;
import com.livevote.entity.VotingChoices;
import com.livevote.repository.VotingProposalRepository;
import com.livevote.service.interfac.ProposalServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import static com.livevote.utils.Utility.*;

@Service
public class ProposalServiceImpl implements ProposalServiceInterface {

    @Autowired
    private VotingProposalRepository votingProposalRepository;

    public Response createProposal(ProposalRequest proposalRequest) {
        Response response = new Response();

        if (!ObjectUtils.isEmpty(proposalRequest)) {
            VotingProposal votingProposal = new VotingProposal();
            votingProposal.setTitle(proposalRequest.getTitle());
            votingProposal.setBody(proposalRequest.getBody());
            votingProposal.setAvatar(proposalRequest.getAvatar().toString());
            votingProposal.setSymbol(proposalRequest.getSymbol());
            votingProposal.setStartDate(proposalRequest.getStartDate());
            votingProposal.setEndDate(proposalRequest.getEndDate());
            votingProposal.setNumOfQR(proposalRequest.getNumOfQR());

            long currentTime = System.currentTimeMillis();
            if (currentTime < proposalRequest.getStartDate()) {
                votingProposal.setState("pending");
            } else if (currentTime >= proposalRequest.getStartDate() && currentTime <= proposalRequest.getEndDate()) {
                votingProposal.setState("active");
            } else {
                votingProposal.setState("closed");
            }

            List<VotingChoices> choices = new ArrayList<>();
            for (ProposalRequest.ChoiceRequest choiceRequest : proposalRequest.getChoices()) {
                VotingChoices choice = new VotingChoices();
                choice.setName(choiceRequest.getName());
                choice.setVotingProposal(votingProposal);
                choices.add(choice);
            }

            votingProposal.setVotingChoices(choices);

            votingProposalRepository.save(votingProposal);
            response.setStatusCode(STATUS_SUCCESSFUL);
            response.setMessage("Proposal created successfully");
        } else {
            response.setStatusCode(STATUS_INVALID_REQUEST);
            response.setMessage("Invalid proposal request");
        }
        return response;
    }

}
