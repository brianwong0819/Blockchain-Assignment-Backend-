package com.livevote.repository;

import com.livevote.entity.VotingProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotingProposalRepository extends JpaRepository<VotingProposal, Long> {
    VotingProposal findByProposalId(String proposalId);
    List<VotingProposal> findByState(String state);
}
