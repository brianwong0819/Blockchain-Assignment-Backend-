package com.livevote.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Data
@Table(name = "voting_choices")
public class VotingChoices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String avatar;

    @ManyToOne
    @JoinColumn(name = "voting_proposal_id")
    private VotingProposal votingProposal;
}
