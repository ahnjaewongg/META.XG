package com.lolservice.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.lolservice.service.MatchService;
import com.lolservice.dto.MatchDTO;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8080" }, allowedHeaders = "*", methods = {
        RequestMethod.GET })
public class MatchController {

    @Autowired
    private MatchService matchService;

    // 매치 데이터 캐시 (소환사 이름 -> 매치 목록)
    private final Map<String, CacheEntry<List<MatchDTO>>> matchCache = new ConcurrentHashMap<>();

    // 캐시 만료 시간 (10분)
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(10);

    @GetMapping("/by-summoner/{summonerName}")
    public ResponseEntity<?> getMatchHistory(@PathVariable String summonerName) {
        try {
            // 캐시에서 데이터 확인
            CacheEntry<List<MatchDTO>> cacheEntry = matchCache.get(summonerName);
            if (cacheEntry != null && !cacheEntry.isExpired()) {
                System.out.println("Using cached match data for " + summonerName);
                return ResponseEntity.ok(cacheEntry.getData());
            }

            // 캐시가 없거나 만료된 경우 새로운 데이터 가져오기
            System.out.println("Fetching new match data for " + summonerName);
            List<MatchDTO> matches = matchService.getMatchHistory(summonerName);

            // 캐시 저장
            matchCache.put(summonerName, new CacheEntry<>(matches, System.currentTimeMillis() + CACHE_TTL_MS));

            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh/{summonerName}")
    public ResponseEntity<List<MatchDTO>> refreshMatchHistory(@PathVariable String summonerName) {
        try {
            // 캐시 삭제
            matchCache.remove(summonerName);

            // 새로운 데이터 가져오기
            List<MatchDTO> matches = matchService.refreshMatchHistory(summonerName);

            // 캐시 업데이트
            matchCache.put(summonerName, new CacheEntry<>(matches, System.currentTimeMillis() + CACHE_TTL_MS));

            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 캐시 항목을 위한 내부 클래스
    private static class CacheEntry<T> {
        private final T data;
        private final long expiryTime;

        public CacheEntry(T data, long expiryTime) {
            this.data = data;
            this.expiryTime = expiryTime;
        }

        public T getData() {
            return data;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}