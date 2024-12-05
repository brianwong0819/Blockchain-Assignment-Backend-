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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.livevote.utils.HashUtils;
import com.livevote.utils.Utility;
import org.apache.commons.lang3.StringUtils;
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

        List<String> choiceNames = proposalRequest.getChoices().stream().map(ProposalRequest.ChoiceRequest::getName).toList();
        if (choiceNames.size() != choiceAvatars.size()) {
            throw new IllegalArgumentException("Mismatch between choice names and avatars");
        } else {
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
                List<String> choiceIds = votingChoicesRepository.findChoiceIdsByProposalId(proposalId);

                String blockchainResponse = this.votingService.createRoom(proposalId, proposalRequest.getNumOfQR(), choiceIds);
                System.out.println("Blockchain Response: " + blockchainResponse);

                return Response.builder()
                        .statusCode(Utility.STATUS_SUCCESSFUL)
                        .message("Proposal and choices created successfully, and voting room created on blockchain")
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create blockchain voting room: " + e.getMessage(), e);
            }

        }
    }

    @Override
    public ProposalDetailsResponse viewProposalDetails(String proposalId) {
        if (StringUtils.isNotEmpty(proposalId)) {

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
        } else {
            return null;
        }
    }

    @Override
    public List<ProposalDetailsResponse> viewAllProposals() {
        List<VotingProposal> proposals = votingProposalRepository.findAll();

        List<VotingProposal> updatedProposals = new ArrayList<>();

        List<ProposalDetailsResponse> responseList = proposals.stream()
                .sorted(Comparator.comparing(VotingProposal::getCreateDate).reversed())
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
        List<VotingResult> results = votingResultRepository.findUnusedQRCodesByProposalId(proposalId);

        return results.stream()
                .map(VotingResult::getQrCode)
                .collect(Collectors.toList());
    }

    @Override
    public Response updateQrStatus(String proposalId, String qrCode, String userWalletAddress) {
        if (StringUtils.isNotEmpty(proposalId) && StringUtils.isNotEmpty(proposalId) && StringUtils.isNotEmpty(userWalletAddress)) {
            VotingResult votingResult = votingResultRepository.findByProposalIdAndQrCode(proposalId, qrCode);
            if (ObjectUtils.isEmpty(votingResult)) {
                return Response.builder()
                        .statusCode(Utility.STATUS_NOT_FOUND)
                        .message("QR Code is not for the given proposal ID.")
                        .build();
            } else {
                votingResult.setUserAddress(userWalletAddress);
                votingResult.setStatus(true);
                votingResultRepository.save(votingResult);

                return Response.builder()
                        .statusCode(Utility.STATUS_SUCCESSFUL)
                        .message("Qr Code Status is updated successfully")
                        .build();
            }
        } else {
            return Response.builder()
                    .statusCode(Utility.STATUS_ERROR)
                    .message("ProposalId or QrCode or User wallet address is missing")
                    .build();
        }
    }

    @Override
    public Response saveVotingResult(String proposalId, String userWalletAddress, String choiceId) {
        if (StringUtils.isNotEmpty(proposalId) && StringUtils.isNotEmpty(userWalletAddress) && StringUtils.isNotEmpty(choiceId)) {
            VotingResult votingResult = votingResultRepository.findByProposalIdAndUserAddress(proposalId, userWalletAddress);

            if (votingResult != null) {
                votingResult.setChoiceId(choiceId);
                votingResult.setVoteTimestamp(System.currentTimeMillis() / 1000);
                votingResultRepository.save(votingResult);

                return Response.builder()
                        .statusCode(Utility.STATUS_SUCCESSFUL)
                        .message("Voting result is saved successfully.")
                        .build();
            } else {
                return Response.builder()
                        .statusCode(Utility.STATUS_NOT_FOUND)
                        .message("Voting result not found for the given proposal ID and user wallet address.")
                        .build();
            }
        } else {
            return Response.builder()
                    .statusCode(Utility.STATUS_INVALID_REQUEST)
                    .message("Invalid request. Please provide all required fields.")
                    .build();
        }
    }


    @Override
    public List<String> getUserVotedProposals(String userWalletAddress) {
        if (StringUtils.isNotEmpty(userWalletAddress)) {
            List<String> votedProposals = votingResultRepository.findByUserAddressAndStatusIsTrue(userWalletAddress);
            return votedProposals.isEmpty() ? null : votedProposals;
        }
        return null;
    }

    @Override
    public Response validateQrStatus(String qrCode, String proposalId) {
        if (StringUtils.isEmpty(qrCode) || StringUtils.isEmpty(proposalId)) {
            throw new IllegalArgumentException("QR Code and Proposal Id cannot be null or empty");
        }

        VotingResult votingResult = votingResultRepository.findByProposalIdAndQrCode(proposalId, qrCode);
        if (!ObjectUtils.isEmpty(votingResult)) {
            if (votingResult.getStatus()) {
                return Response.builder()
                        .statusCode(Utility.STATUS_SUCCESSFUL)
                        .message("true")
                        .build();
            } else {
                return Response.builder()
                        .statusCode(Utility.STATUS_SUCCESSFUL)
                        .message("false")
                        .build();
            }
        } else {
            return Response.builder()
                    .statusCode(Utility.STATUS_NOT_FOUND)
                    .message("QR Code is not for the given proposal ID.")
                    .build();
        }
    }

    @Override
    public List<VotingResultResponse> getVotingResult(String proposalId, String userWalletAddress) {
        if (StringUtils.isEmpty(proposalId)) {
            throw new IllegalArgumentException("Proposal ID cannot be null or empty");
        }

        List<VotingResult> results;

        if (StringUtils.isNotEmpty(userWalletAddress)) {
            VotingResult result = votingResultRepository.findByProposalIdAndUserAddress(proposalId, userWalletAddress);
            results = result != null ? List.of(result) : List.of();
        } else {
            results = votingResultRepository.findByProposalId(proposalId);
        }

        return results.stream()
                .sorted(Comparator.comparing(VotingResult::getVoteTimestamp))
                .map(result -> VotingResultResponse.builder()
                        .userWalletAddress(result.getUserAddress())
                        .choiceId(result.getChoiceId())
                        .voteTimestamp(result.getVoteTimestamp())
                        .build())
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
