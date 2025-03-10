package com.lolservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lolservice.service.MatchService;
import com.lolservice.dto.MatchDTO;
import java.util.Map;
import com.lolservice.dto.LeagueEntryDTO;
import com.lolservice.service.SummonerService;
import com.lolservice.service.RiotApiService;
import com.lolservice.dto.SummonerDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/summoner")
@CrossOrigin(origins = "http://localhost:3000")
public class SummonerController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private SummonerService summonerService;

    @Autowired
    private RiotApiService riotApiService;

    // 각 타입의 데이터에 대한 캐시 맵
    private final Map<String, CacheEntry<SummonerDTO>> summonerCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<LeagueEntryDTO>> rankCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<Object>> currentGameCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<List<MatchDTO>>> matchesCache = new ConcurrentHashMap<>();

    // 캐시 만료 시간 (10분)
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(10);

    @GetMapping("/{summonerName}")
    public ResponseEntity<?> getSummonerInfo(@PathVariable String summonerName) {
        try {
            // 캐시에서 데이터 확인
            CacheEntry<SummonerDTO> cacheEntry = summonerCache.get(summonerName);
            if (cacheEntry != null && !cacheEntry.isExpired()) {
                System.out.println("Using cached summoner data for " + summonerName);
                return ResponseEntity.ok(cacheEntry.getData());
            }

            // 캐시가 없거나 만료된 경우 새로운 데이터 가져오기
            System.out.println("Fetching new summoner data for " + summonerName);
            // URL 디코딩 및 Riot ID 처리
            String decodedName = java.net.URLDecoder.decode(summonerName, "UTF-8");
            SummonerDTO summoner = riotApiService.getSummonerByName(decodedName);

            // 소환사 정보 데이터베이스에 저장
            summonerService.saveSummoner(summoner);

            // 캐시 저장
            summonerCache.put(summonerName, new CacheEntry<>(summoner, System.currentTimeMillis() + CACHE_TTL_MS));

            return ResponseEntity.ok(summoner);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{summonerName}/matches")
    public ResponseEntity<?> getSummonerMatches(@PathVariable String summonerName) {
        try {
            // 캐시에서 데이터 확인
            CacheEntry<List<MatchDTO>> cacheEntry = matchesCache.get(summonerName);
            if (cacheEntry != null && !cacheEntry.isExpired()) {
                System.out.println("Using cached matches data for " + summonerName);
                return ResponseEntity.ok(cacheEntry.getData());
            }

            // 캐시가 없거나 만료된 경우 새로운 데이터 가져오기
            System.out.println("Fetching new matches data for " + summonerName);
            String decodedName = java.net.URLDecoder.decode(summonerName, "UTF-8");
            SummonerDTO summoner = riotApiService.getSummonerByName(decodedName);

            // 최근 매치 목록 조회
            List<String> matchIds = riotApiService.getMatchIdsByPuuid(summoner.getPuuid());

            // 매치 상세 정보 조회 및 저장
            List<MatchDTO> matches = matchIds.stream()
                    .map(id -> {
                        MatchDTO match = riotApiService.getMatchById(id);
                        if (match != null) {
                            // 매치 참가자 정보 저장
                            summonerService.saveMatchParticipants(match, decodedName);
                        }
                        return match;
                    })
                    .collect(Collectors.toList());

            // 캐시 저장
            matchesCache.put(summonerName, new CacheEntry<>(matches, System.currentTimeMillis() + CACHE_TTL_MS));

            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/rank/{summonerId}")
    public ResponseEntity<LeagueEntryDTO> getSummonerRank(@PathVariable String summonerId) {
        try {
            // 캐시에서 데이터 확인
            CacheEntry<LeagueEntryDTO> cacheEntry = rankCache.get(summonerId);
            if (cacheEntry != null && !cacheEntry.isExpired()) {
                System.out.println("Using cached rank data for " + summonerId);
                return ResponseEntity.ok(cacheEntry.getData());
            }

            // 캐시가 없거나 만료된 경우 새로운 데이터 가져오기
            System.out.println("Fetching new rank data for " + summonerId);
            LeagueEntryDTO rank = matchService.getSummonerRank(summonerId);

            // 캐시 저장
            rankCache.put(summonerId, new CacheEntry<>(rank, System.currentTimeMillis() + CACHE_TTL_MS));

            return ResponseEntity.ok(rank);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/current-game/{summonerId}")
    public ResponseEntity<?> getCurrentGame(@PathVariable String summonerId) {
        try {
            // 현재 게임 데이터는 짧은 시간 (2분) 동안만 캐싱
            long currentGameTTL = TimeUnit.MINUTES.toMillis(2);

            // 캐시에서 데이터 확인
            CacheEntry<Object> cacheEntry = currentGameCache.get(summonerId);
            if (cacheEntry != null && !cacheEntry.isExpired()) {
                System.out.println("Using cached current game data for " + summonerId);
                return ResponseEntity.ok(cacheEntry.getData());
            }

            // 캐시가 없거나 만료된 경우 새로운 데이터 가져오기
            System.out.println("Fetching new current game data for " + summonerId);
            MatchDTO currentGame = matchService.getCurrentGame(summonerId);

            // 캐시 저장 (현재 게임은 더 짧은 TTL 사용)
            currentGameCache.put(summonerId,
                    new CacheEntry<>(currentGame, System.currentTimeMillis() + currentGameTTL));

            return ResponseEntity.ok(currentGame);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh/{summonerName}")
    public ResponseEntity<?> refreshSummonerData(@PathVariable String summonerName) {
        try {
            // 해당 소환사의 모든 캐시 데이터 삭제
            summonerCache.remove(summonerName);

            // 소환사 정보 다시 가져오기
            String decodedName = java.net.URLDecoder.decode(summonerName, "UTF-8");
            SummonerDTO summoner = riotApiService.getSummonerByName(decodedName);
            summonerService.saveSummoner(summoner);

            // 관련된 다른 캐시도 삭제
            if (summoner != null) {
                rankCache.remove(summoner.getId());
                currentGameCache.remove(summoner.getId());
            }
            matchesCache.remove(summonerName);

            // 새 데이터 캐싱
            summonerCache.put(summonerName, new CacheEntry<>(summoner, System.currentTimeMillis() + CACHE_TTL_MS));

            Map<String, Object> response = new HashMap<>();
            response.put("message", "캐시가 성공적으로 새로고침되었습니다.");
            response.put("summoner", summoner);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
