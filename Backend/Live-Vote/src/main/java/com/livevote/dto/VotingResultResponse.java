package com.livevote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class VotingResultResponse {
    private String qrCode;
}
