package com.livevote.service.impl;

import com.livevote.dto.ProposalDetailsResponse;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProposalServiceImpl implements ProposalServiceInterface {

    @Autowired
    private VotingProposalRepository votingProposalRepository;

    @Autowired
    private VotingChoicesRepository votingChoicesRepository;

    @Override
    public Response createProposal(ProposalRequest proposalRequest, MultipartFile avatar, List<MultipartFile> choiceAvatars) throws Exception {
        String avatarPath = saveFile(avatar);
        String state = determineState(proposalRequest.getStartDate(), proposalRequest.getEndDate());
        String proposalId = generateProposalId();

        VotingProposal votingProposal = VotingProposal.builder()
                .proposalId(proposalId)
                .title(proposalRequest.getTitle())
                .body(proposalRequest.getBody())
                .avatar(avatarPath)
                .symbol(proposalRequest.getSymbol())
                .startDate(proposalRequest.getStartDate())
                .endDate(proposalRequest.getEndDate())
                .state(state)
                .numOfQR(proposalRequest.getNumOfQR())
                .createDate(proposalRequest.getCreateDate())
                .build();

        votingProposalRepository.save(votingProposal);

        List<String> choiceNames = proposalRequest.getChoices()
                .stream().map(ProposalRequest.ChoiceRequest::getName).toList();

        if (choiceNames.size() != choiceAvatars.size()) {
            throw new IllegalArgumentException("Mismatch between choice names and avatars");
        }

        List<VotingChoices> choicesList = new ArrayList<>();
        for (int i = 0; i < choiceNames.size(); i++) {
            VotingChoices choice = VotingChoices.builder()
                    .name(choiceNames.get(i))
                    .avatar(saveFile(choiceAvatars.get(i)))
                    .votingProposal(votingProposal)
                    .build();

            choicesList.add(choice);
        }

        votingChoicesRepository.saveAll(choicesList);

        return Response.builder()
                .statusCode(200)
                .message("Proposal and choices created successfully")
                .build();
    }


    @Override
    public ProposalDetailsResponse viewProposalDetails(String proposalId) {
        VotingProposal proposal = votingProposalRepository.findByProposalId(proposalId);

        List<ProposalDetailsResponse.ChoiceDetails> choiceDetails = proposal.getVotingChoices().stream()
                .map(choice -> ProposalDetailsResponse.ChoiceDetails.builder()
                        .name(choice.getName())
                        .avatarUrl("/uploads/" + choice.getAvatar())
                        .build())
                .collect(Collectors.toList());

        return ProposalDetailsResponse.builder()
                .proposalId(proposal.getProposalId())
                .title(proposal.getTitle())
                .body(proposal.getBody())
                .symbol(proposal.getSymbol())
                .startDate(proposal.getStartDate())
                .endDate(proposal.getEndDate())
                .state(proposal.getState())
                .numOfQR(proposal.getNumOfQR())
                .createDate(proposal.getCreateDate())
                .avatarUrl("/uploads/" + proposal.getAvatar())
                .choices(choiceDetails)
                .build();
    }


    private String saveFile(MultipartFile file) throws Exception {
        Path uploadDirectory = Paths.get("Backend/Live-Vote/uploads");
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

    private String determineState(Long startDate, Long endDate) {
        long currentTime = System.currentTimeMillis();

        if (currentTime < startDate) {
            return "pending";
        } else if (currentTime >= startDate && currentTime <= endDate) {
            return "active";
        } else {
            return "closed";
        }
    }

    private String generateProposalId() {
        long count = votingProposalRepository.count();
        DecimalFormat format = new DecimalFormat("00000");
        return "PRP_" + format.format(count + 1);
    }

    @Override
    public List<ProposalDetailsResponse> viewAllProposals() {
        List<VotingProposal> proposals = votingProposalRepository.findAll();

        return proposals.stream()
                .map(proposal -> {
                    List<ProposalDetailsResponse.ChoiceDetails> choiceDetails = proposal.getVotingChoices().stream()
                            .map(choice -> ProposalDetailsResponse.ChoiceDetails.builder()
                                    .name(choice.getName())
                                    .avatarUrl("/uploads/" + choice.getAvatar())
                                    .build())
                            .collect(Collectors.toList());

                    return ProposalDetailsResponse.builder()
                            .proposalId(proposal.getProposalId())
                            .title(proposal.getTitle())
                            .body(proposal.getBody())
                            .symbol(proposal.getSymbol())
                            .state(proposal.getState())
                            .startDate(proposal.getStartDate())
                            .endDate(proposal.getEndDate())
                            .numOfQR(proposal.getNumOfQR())
                            .createDate(proposal.getCreateDate())
                            .avatarUrl("/uploads/" + proposal.getAvatar())
                            .choices(choiceDetails)
                            .build();
                })
                .collect(Collectors.toList());
    }


}