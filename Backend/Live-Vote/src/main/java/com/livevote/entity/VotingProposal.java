package com.livevote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "voting_proposal")
public class VotingProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String proposalId;
    private String title;

    @Column(length = 3000)
    private String body;

    private String avatar;
    private String symbol;
    private String state;
    private Long startDate;
    private Long endDate;
    private int numOfQR;
    private Long createDate;

    @OneToMany(mappedBy = "votingProposal", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<VotingChoices> votingChoices = new ArrayList<>();

}
