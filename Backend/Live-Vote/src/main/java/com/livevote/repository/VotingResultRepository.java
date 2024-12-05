package com.livevote.repository;

import com.livevote.entity.VotingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface VotingResultRepository extends JpaRepository<VotingResult, Long> {
    @Query("SELECT vr FROM VotingResult vr WHERE vr.proposalId = :proposalId AND vr.status = false")
    List<VotingResult> findUnusedQRCodesByProposalId(@Param("proposalId") String proposalId);

    @Query("SELECT vr FROM VotingResult vr WHERE vr.proposalId = :proposalId AND vr.qrCode = :qrCode")
    VotingResult findByProposalIdAndQrCode(@Param("proposalId") String proposalId, @Param("qrCode") String qrCode);

    @Query("SELECT vr FROM VotingResult vr WHERE vr.proposalId = :proposalId AND vr.userAddress = :userAddress")
    VotingResult findByProposalIdAndUserAddress(@Param("proposalId") String proposalId, @Param("userAddress") String userAddress);

    @Query("SELECT v.proposalId FROM VotingResult v WHERE v.userAddress = :userAddress AND v.status = true AND v.choiceId IS NOT NULL")
    List<String> findByUserAddressAndStatusIsTrueAndChoiceIdNotNull(@Param("userAddress") String userAddress);

    @Query("SELECT vr FROM VotingResult vr WHERE vr.proposalId = :proposalId AND vr.userAddress = :userAddress AND vr.choiceId IS NOT NULL")
    List<VotingResult> findByProposalIdAndUserAddressWithNonNullChoiceId(@Param("proposalId") String proposalId, @Param("userAddress") String userWalletAddress);

    @Query("SELECT vr FROM VotingResult vr WHERE vr.proposalId = :proposalId AND vr.choiceId IS NOT NULL")
    List<VotingResult> findByProposalIdWithNonNullChoiceId(@Param("proposalId") String proposalId);
}
