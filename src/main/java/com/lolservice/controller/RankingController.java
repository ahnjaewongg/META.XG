package com.lolservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lolservice.repository.RankingRepository;
import com.lolservice.entity.RankingEntity;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8080" }, allowedHeaders = "*", methods = {
        RequestMethod.GET }, allowCredentials = "true")
public class RankingController {
    private final RankingRepository rankingRepository;
    private static final Logger log = LoggerFactory.getLogger(RankingController.class);

    @GetMapping("/rankings")
    public ResponseEntity<Page<RankingEntity>> getAllRankings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            log.info("Fetching rankings for page {} with size {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<RankingEntity> rankings = rankingRepository.findAllByOrderByLeaguePointsDesc(pageable);
            return ResponseEntity.ok(rankings);
        } catch (Exception e) {
            log.error("Error fetching rankings: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}