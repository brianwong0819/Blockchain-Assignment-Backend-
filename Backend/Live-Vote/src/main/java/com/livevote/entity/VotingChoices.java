package com.livevote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "voting_choices")
public class VotingChoices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String choiceId;
    private String name;
    private String avatar;

    @ManyToOne
    @JoinColumn(name = "voting_proposal_id")
    private VotingProposal votingProposal;
}
