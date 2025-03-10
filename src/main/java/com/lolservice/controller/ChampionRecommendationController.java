package com.lolservice.controller;

import com.lolservice.service.ChampionRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
@RequiredArgsConstructor
public class ChampionRecommendationController {
    private final ChampionRecommendationService recommendationService;

    @GetMapping("/{summonerName}")
    public ResponseEntity<?> getRecommendations(@PathVariable String summonerName) {
        try {
            return ResponseEntity.ok(recommendationService.getRecommendations(summonerName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}