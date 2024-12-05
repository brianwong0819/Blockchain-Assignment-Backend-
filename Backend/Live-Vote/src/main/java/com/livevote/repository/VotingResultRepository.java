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

    @Query("SELECT v.proposalId FROM VotingResult v WHERE v.userAddress = :userAddress AND v.status = true")
    List<String> findByUserAddressAndStatusIsTrue(@Param("userAddress") String userAddress);

    VotingResult findByQrCode(String qrCode);

    List<VotingResult> findByProposalId(String proposalId);
}
