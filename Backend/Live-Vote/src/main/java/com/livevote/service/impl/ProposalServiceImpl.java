package com.livevote.service.impl;

import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import com.livevote.entity.VotingProposal;
import com.livevote.entity.VotingChoices;
import com.livevote.repository.VotingChoicesRepository;
import com.livevote.repository.VotingProposalRepository;
import com.livevote.service.interfac.ProposalServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProposalServiceImpl implements ProposalServiceInterface {

    @Autowired
    private VotingProposalRepository votingProposalRepository;

    @Autowired
    private VotingChoicesRepository votingChoicesRepository;

    @Override
    public Response createProposal(ProposalRequest proposalRequest, MultipartFile avatar, List<MultipartFile> choiceAvatars) throws Exception {
        String avatarPath = saveFile(avatar);

        VotingProposal votingProposal = new VotingProposal();
        votingProposal.setTitle(proposalRequest.getTitle());
        votingProposal.setBody(proposalRequest.getBody());
        votingProposal.setAvatar(avatarPath);
        votingProposal.setSymbol(proposalRequest.getSymbol());
        votingProposal.setStartDate(proposalRequest.getStartDate());
        votingProposal.setEndDate(proposalRequest.getEndDate());
        votingProposal.setNumOfQR(proposalRequest.getNumOfQR());

        votingProposalRepository.save(votingProposal);

        List<String> choiceNames = proposalRequest.getChoices()
                .stream().map(ProposalRequest.ChoiceRequest::getName).toList();

        if (choiceNames.size() != choiceAvatars.size()) {
            throw new IllegalArgumentException("Mismatch between choice names and avatars");
        }

        List<VotingChoices> choicesList = new ArrayList<>();
        for (int i = 0; i < choiceNames.size(); i++) {
            VotingChoices choice = new VotingChoices();
            choice.setName(choiceNames.get(i));

            String choiceAvatarPath = saveFile(choiceAvatars.get(i));
            choice.setAvatar(choiceAvatarPath);

            choice.setVotingProposal(votingProposal);

            choicesList.add(choice);
        }

        votingChoicesRepository.saveAll(choicesList);

        Response response = new Response();
        response.setStatusCode(200);
        response.setMessage("Proposal and choices created successfully");
        return response;
    }

    private String saveFile(MultipartFile file) throws Exception {
        Path uploadDirectory = Paths.get("uploads");
        if (!Files.exists(uploadDirectory)) {
            Files.createDirectories(uploadDirectory);
        }
        if (!ObjectUtils.isEmpty(file.getOriginalFilename())) {
            Path filePath = uploadDirectory.resolve(file.getOriginalFilename());
            file.transferTo(filePath);
            return filePath.toString();
        }
        return null;
    }

}
