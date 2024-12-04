package com.livevote.repository;

import com.livevote.entity.VotingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface VotingResultRepository extends JpaRepository<VotingResult, Long> {
    List<VotingResult> findByProposalId(String proposalId);

    VotingResult findByProposalIdAndQrCode(String proposalId, String qrCode);

    VotingResult findByProposalIdAndUserAddress(String proposalId, String userAddress);
}
