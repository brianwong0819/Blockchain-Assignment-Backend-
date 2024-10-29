package com.livevote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalDetailsResponse {
    private String proposalId;
    private String title;
    private String body;
    private String symbol;
    private String state;
    private Long startDate;
    private Long endDate;
    private int numOfQR;
    private Long createDate;
    private String avatarUrl;
    private List<ChoiceDetails> choices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChoiceDetails {
        private String name;
        private String avatarUrl;
    }
}
