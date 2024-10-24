package com.livevote.repository;

import com.livevote.entity.VotingProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VotingProposalRepository extends JpaRepository<VotingProposal, Long> {
}
