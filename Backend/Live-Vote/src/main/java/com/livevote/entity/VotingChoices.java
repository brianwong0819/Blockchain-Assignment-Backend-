package com.livevote.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "voting_choices")
public class VotingChoices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String avatar;
    private Integer score;

    @ManyToOne
    @JoinColumn(name = "voting_proposal_id")
    private VotingProposal votingProposal;
}
