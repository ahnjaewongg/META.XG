package com.lolservice.controller;

import com.lolservice.service.MentalAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 소환사의 심리적 상태를 분석하고 멘탈 케어 추천사항을 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/mental")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class MentalAnalysisController {

    private final MentalAnalysisService mentalAnalysisService;

    /**
     * 소환사 이름을 기반으로 멘탈 상태 분석 및 추천사항 제공
     * 
     * @param summonerName 소환사 이름
     * @return 멘탈 분석 결과 및 추천사항
     */
    @GetMapping("/analyze/{summonerName}")
    public ResponseEntity<Map<String, Object>> analyzeMentalState(@PathVariable String summonerName) {
        log.info("Request received to analyze mental state for summoner: {}", summonerName);

        Map<String, Object> result = mentalAnalysisService.analyzeMentalState(summonerName);

        log.info("Mental analysis result for summoner {}: {}", summonerName, result);

        if (result.containsKey("error")) {
            log.warn("Error analyzing mental state for summoner: {}, error: {}", summonerName, result.get("error"));
            return ResponseEntity.badRequest().body(result);
        }

        log.info("Mental state analysis completed for summoner: {}", summonerName);
        return ResponseEntity.ok(result);
    }
}