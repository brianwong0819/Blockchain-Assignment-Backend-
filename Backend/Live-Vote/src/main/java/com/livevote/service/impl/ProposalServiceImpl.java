package com.livevote.service.impl;

import com.livevote.dto.ProposalDetailsResponse;
import com.livevote.dto.ProposalRequest;
import com.livevote.dto.Response;
import com.livevote.dto.VotingResultResponse;
import com.livevote.entity.VotingChoices;
import com.livevote.entity.VotingProposal;
import com.livevote.entity.VotingResult;
import com.livevote.repository.VotingChoicesRepository;
import com.livevote.repository.VotingProposalRepository;
import com.livevote.repository.VotingResultRepository;
import com.livevote.service.interfac.BlockchainServiceInterface;
import com.livevote.service.interfac.ProposalServiceInterface;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.livevote.utils.HashUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProposalServiceImpl implements ProposalServiceInterface {
    @Autowired
    private VotingProposalRepository votingProposalRepository;
    @Autowired
    private VotingChoicesRepository votingChoicesRepository;
    @Autowired
    private BlockchainServiceInterface votingService;
    @Autowired
    private VotingResultRepository votingResultRepository;

    public ProposalServiceImpl() {
    }

    public Response createProposal(ProposalRequest proposalRequest, MultipartFile avatar, List<MultipartFile> choiceAvatars) throws Exception {
        String avatarPath = this.saveFile(avatar);
        String state = this.determineState(proposalRequest.getStartDate(), proposalRequest.getEndDate());
        String proposalId = this.generateProposalId();
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
        this.votingProposalRepository.save(votingProposal);
        List<String> choiceNames = proposalRequest.getChoices().stream().map(ProposalRequest.ChoiceRequest::getName).toList();
        if (choiceNames.size() != choiceAvatars.size()) {
            throw new IllegalArgumentException("Mismatch between choice names and avatars");
        } else {
            List<VotingChoices> choicesList = new ArrayList();

            for(int i = 0; i < choiceNames.size(); ++i) {
                VotingChoices choice = VotingChoices.builder()
                        .name((String)choiceNames.get(i))
                        .avatar(this.saveFile((MultipartFile)choiceAvatars.get(i)))
                        .votingProposal(votingProposal)
                        .build();
                choicesList.add(choice);
            }
            this.votingChoicesRepository.saveAll(choicesList);

            List<VotingResult> qrEntries = new ArrayList<>();
            for (int i = 0; i < proposalRequest.getNumOfQR(); i++) {
                String qrString = proposalId + "_QR_" + String.format("%04d", i + 1);
                String hashedQrString = HashUtils.hashString(qrString);
                VotingResult qrEntry = VotingResult.builder()
                        .proposalId(proposalId)
                        .qrCode(hashedQrString)
                        .status(false)
                        .build();
                qrEntries.add(qrEntry);
            }
            votingResultRepository.saveAll(qrEntries);

            try {
                List<Integer> candidateIds = new ArrayList();

                int roomId;
                for(roomId = 0; roomId < choiceNames.size(); ++roomId) {
                    candidateIds.add(roomId + 1);
                }

                roomId = Integer.parseInt(proposalId.substring(4));
                String blockchainResponse = this.votingService.createRoom(proposalId, proposalRequest.getNumOfQR(), candidateIds);
                System.out.println("Blockchain Response: " + blockchainResponse);
                return Response.builder().statusCode(200).message("Proposal and choices created successfully, and voting room created on blockchain").build();
            } catch (Exception var13) {
                throw new RuntimeException("Failed to create blockchain voting room: " + var13.getMessage(), var13);
            }
        }
    }

    @Override
    public ProposalDetailsResponse viewProposalDetails(String proposalId) {
        VotingProposal proposal = votingProposalRepository.findByProposalId(proposalId);

        List<ProposalDetailsResponse.ChoiceDetails> choiceDetails = proposal.getVotingChoices().stream()
                .map(choice -> ProposalDetailsResponse.ChoiceDetails.builder()
                        .choiceId(choice.getChoiceId())
                        .name(choice.getName())
                        .avatar(choice.getAvatar())
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
                .avatar(proposal.getAvatar())
                .choices(choiceDetails)
                .build();
    }

    @Override
    public List<ProposalDetailsResponse> viewAllProposals() {
        List<VotingProposal> proposals = votingProposalRepository.findAll();

        List<VotingProposal> updatedProposals = new ArrayList<>();

        List<ProposalDetailsResponse> responseList = proposals.stream()
                .map(proposal -> {
                    // Check and update the state
                    String newState = determineState(proposal.getStartDate(), proposal.getEndDate());
                    if (!newState.equals(proposal.getState())) {
                        proposal.setState(newState);
                        updatedProposals.add(proposal);
                    }

                    List<ProposalDetailsResponse.ChoiceDetails> choiceDetails = proposal.getVotingChoices().stream()
                            .map(choice -> ProposalDetailsResponse.ChoiceDetails.builder()
                                    .name(choice.getName())
                                    .avatar(choice.getAvatar())
                                    .build())
                            .collect(Collectors.toList());

                    return ProposalDetailsResponse.builder()
                            .proposalId(proposal.getProposalId())
                            .title(proposal.getTitle())
                            .body(proposal.getBody())
                            .symbol(proposal.getSymbol())
                            .state(newState) // Use updated state
                            .startDate(proposal.getStartDate())
                            .endDate(proposal.getEndDate())
                            .numOfQR(proposal.getNumOfQR())
                            .createDate(proposal.getCreateDate())
                            .avatar(proposal.getAvatar())
                            .choices(choiceDetails)
                            .build();
                })
                .collect(Collectors.toList());

        if (!updatedProposals.isEmpty()) {
            votingProposalRepository.saveAll(updatedProposals);
        }

        return responseList;
    }
    @Override
    public List<String> getTokenQr(String proposalId) {
        List<VotingResult> results = votingResultRepository.findByProposalId(proposalId);

        return results.stream()
                .map(VotingResult::getQrCode)
                .collect(Collectors.toList());
    }


    private String saveFile(MultipartFile file) throws Exception {
        Path uploadDirectory = Paths.get("uploads");
        if (!Files.exists(uploadDirectory, new LinkOption[0])) {
            Files.createDirectories(uploadDirectory);
        }

        if (!ObjectUtils.isEmpty(file.getOriginalFilename())) {
            Path filePath = uploadDirectory.resolve(file.getOriginalFilename());
            file.transferTo(filePath);
            return filePath.toString();
        } else {
            return null;
        }
    }

    private String determineState(Long startDate, Long endDate) {
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime < startDate) {
            return "pending";
        } else {
            return currentTime >= startDate && currentTime <= endDate ? "active" : "closed";
        }
    }

    private String generateProposalId() {
        long count = this.votingProposalRepository.count();
        DecimalFormat format = new DecimalFormat("00000");
        String var10000 = format.format(count + 1L);
        return "PRP_" + var10000;
    }
}
