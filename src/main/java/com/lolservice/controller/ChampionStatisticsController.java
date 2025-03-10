package com.lolservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

import com.lolservice.service.ChampionStatisticsService;
import com.lolservice.dto.ChampionStatisticsResponse;
import com.lolservice.entity.ChampionStatisticsEntity;

@RestController
@RequestMapping("/api/champions")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8080" }, allowedHeaders = "*", methods = {
        RequestMethod.GET })
@Slf4j
public class ChampionStatisticsController {
    @Autowired
    private ChampionStatisticsService championStatisticsService;

    @GetMapping("/statistics")
    public ResponseEntity<?> getChampionStatistics() {
        try {
            List<ChampionStatisticsEntity> stats = championStatisticsService.calculateChampionStatistics();

            List<ChampionStatisticsResponse> response = stats.stream()
                    .map(stat -> {
                        // Double을 primitive double로 안전하게 변환
                        double winRate = stat.getWinRate();
                        double pickRate = stat.getPickRate();
                        double banRate = stat.getBanRate();

                        return new ChampionStatisticsResponse(
                                stat.getChampionId(),
                                stat.getChampionName(),
                                stat.getNameKo(),
                                stat.getImageUrl(),
                                stat.getPositions(),
                                winRate,
                                pickRate,
                                banRate,
                                stat.getTier());
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching champion statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching champion statistics: " + e.getMessage()));
        }
    }
}