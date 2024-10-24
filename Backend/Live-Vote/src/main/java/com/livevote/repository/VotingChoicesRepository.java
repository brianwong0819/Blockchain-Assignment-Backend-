package com.livevote.repository;

import com.livevote.entity.VotingChoices;
import com.livevote.entity.VotingProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VotingChoicesRepository extends JpaRepository<VotingChoices, Long> {
}
