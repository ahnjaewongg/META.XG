package com.lolservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lolservice.repository.MatchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataCleanupService {
    private final MatchRepository matchRepository;

    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 자정 실행
    @Transactional
    public void cleanupOldMatches() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(1);
            int deletedCount = matchRepository.deleteByGameCreationBefore(cutoffDate);
            log.info("Deleted {} old matches", deletedCount);
        } catch (Exception e) {
            log.error("Error cleaning up old matches: {}", e.getMessage());
        }
    }
}