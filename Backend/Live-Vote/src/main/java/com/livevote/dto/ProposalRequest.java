package com.livevote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@Data
public class ProposalRequest {

    private String title;
    private String body;
    private MultipartFile avatar;
    private String symbol;
    private Long startDate;
    private Long endDate;
    private int numOfQR;
    private List<ChoiceRequest> choices;

    @Data
    public static class ChoiceRequest {
        private String name;
        private MultipartFile avatar;
    }
}
