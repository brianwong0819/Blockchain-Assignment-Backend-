package com.livevote.repository;

import com.livevote.entity.VotingChoices;
import com.livevote.entity.VotingProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VotingChoicesRepository extends JpaRepository<VotingChoices, Long> {

    @Query("SELECT vc.choiceId FROM VotingChoices vc WHERE vc.votingProposal.proposalId = :proposalId")
    List<String> findChoiceIdsByProposalId(@Param("proposalId") String proposalId);
}
