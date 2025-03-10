package com.lolservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 멘탈 분석 결과 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentalAnalysisResponseDTO {

    private String summonerName;
    private MentalState mentalState;
    private List<String> recommendations;
    private int gamesAnalyzed;
    private double winRate;

    /**
     * 멘탈 상태 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentalState {
        private String status;
        private String message;
        private String explanation;
    }
}