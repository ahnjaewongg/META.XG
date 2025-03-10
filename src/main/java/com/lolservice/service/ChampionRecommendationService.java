package com.lolservice.service;

import com.lolservice.entity.ChallengerParticipantEntity;
import com.lolservice.entity.MatchParticipantEntity;
import com.lolservice.repository.MatchParticipantRepository;
import com.lolservice.repository.MatchRepository;
import com.lolservice.repository.ChampionPositionRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChampionRecommendationService {
    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final ChampionPositionRepository championPositionRepository;

    public Map<String, Object> getRecommendations(String summonerName) {
        Map<String, Object> result = new HashMap<>();

        // 1. 소환사의 최근 매치 데이터 조회
        List<MatchParticipantEntity> recentMatches = matchParticipantRepository.findBySummonerName(summonerName);

        if (recentMatches.isEmpty()) {
            result.put("message", "최근 게임 기록이 없습니다");
            return result;
        }

        // 2. 최근 많이 플레이한 챔피언 분석
        Map<Integer, Long> championCounts = recentMatches.stream()
                .collect(Collectors.groupingBy(
                        MatchParticipantEntity::getChampionId,
                        Collectors.counting()));

        log.info("Champion counts for {}: {}", summonerName, championCounts);

        // 가장 많이 플레이한 챔피언 ID
        Integer mostPlayedChampionId = championCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // mostPlayedChampionId를 String으로 변환 (기존 코드와 호환성 유지)
        String mostPlayedChampion = mostPlayedChampionId != null ? mostPlayedChampionId.toString() : null;

        if (mostPlayedChampion == null) {
            log.warn("Could not determine most played champion for {}", summonerName);
            result.put("message", "플레이한 챔피언을 찾을 수 없습니다");
            return result;
        }

        log.info("Most played champion for {}: {}", summonerName, mostPlayedChampion);
        result.put("mostPlayedChampion", mostPlayedChampion);

        // 4. 해당 챔피언의 주 포지션 찾기
        String position = null;
        try {
            int championId = Integer.parseInt(mostPlayedChampion);
            position = championPositionRepository.findMainPositionByChampionId(championId);
        } catch (NumberFormatException e) {
            log.error("챔피언 ID 변환 오류: {}", mostPlayedChampion, e);
            // 예외 발생 시 기존 메서드 사용 시도
            position = championPositionRepository.findMainPositionByChampionName(mostPlayedChampion);
        }

        if (position == null || position.isEmpty()) {
            // 포지션 정보가 없는 경우 기본값 설정
            position = "MIDDLE"; // 기본값으로 미드 설정
            log.warn("No position found for champion ID: {}, using default position: {}", mostPlayedChampion, position);
        }

        log.info("Position for champion {}: {}", mostPlayedChampion, position);
        result.put("recommendedPosition", position);

        // 5. 같은 포지션의 챌린저 플레이어 데이터 분석 (현재 챔피언 제외)
        List<ChampionStats> championStats = analyzeChampionPerformance(position, mostPlayedChampion);

        // 6. 결과 반환
        result.put("recommendedChampions", championStats.subList(0, Math.min(5, championStats.size())));

        return result;
    }

    private List<ChampionStats> analyzeChampionPerformance(String position, String excludeChampion) {
        // 최근 7일 동안의 챌린저 매치만 분석
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);

        // 챌린저 플레이어들의 해당 라인 챔피언 성능 분석 (현재 챔피언 제외)
        List<ChallengerParticipantEntity> challengers = matchRepository
                .findChallengerMatchesByPositionExcludingChampion(
                        position,
                        excludeChampion,
                        cutoffDate);

        if (challengers.isEmpty()) {
            // 제외할 챔피언이 너무 많은 데이터를 제거하는 경우 기본 메소드로 폴백
            log.warn("No data found when excluding champion {}. Using all position data.", excludeChampion);
            challengers = matchRepository.findChallengerMatches(position, cutoffDate);
        }

        Map<String, ChampionStats> statsMap = new HashMap<>();

        // 챔피언별 성능 집계
        challengers.forEach(p -> {
            ChampionStats stats = statsMap.computeIfAbsent(
                    p.getChampionName(),
                    k -> new ChampionStats(k));

            stats.addGame(p);
        });

        // 수정된 성능 점수 계산 및 정렬 - 승률, 픽률, 밴률 반영
        List<ChampionStats> result = statsMap.values().stream()
                .filter(stats -> stats.getTotalGames() >= 1) // 최소 1게임 이상으로 조건 완화
                .sorted(Comparator.comparingDouble(ChampionStats::getScore).reversed())
                .collect(Collectors.toList());

        // 픽률과 밴률 계산 (총 게임수 대비)
        int totalGames = challengers.size();
        result.forEach(stats -> stats.calculateRates(totalGames));

        // 결과가 없을 경우 처리
        if (result.isEmpty()) {
            log.warn("No champion recommendations found. Adding default recommendations.");
            // 기본 추천 챔피언 추가 (포지션별 인기 챔피언)
            addDefaultRecommendations(result, position);
        }

        return result;
    }

    // 기본 추천 챔피언 추가 메서드
    private void addDefaultRecommendations(List<ChampionStats> result, String position) {
        Map<String, List<String>> defaultChampions = new HashMap<>();
        defaultChampions.put("TOP", Arrays.asList("Darius", "Fiora", "Jax", "Garen", "Sett"));
        defaultChampions.put("JUNGLE", Arrays.asList("Lee Sin", "Vi", "Hecarim", "Warwick", "Amumu"));
        defaultChampions.put("MIDDLE", Arrays.asList("Ahri", "Yasuo", "Lux", "Vex", "Zed"));
        defaultChampions.put("SUPPORT", Arrays.asList("Thresh", "Leona", "Lulu", "Blitzcrank", "Morgana"));
        defaultChampions.put("BOTTOM", Arrays.asList("Jinx", "Caitlyn", "Jhin", "Ezreal", "Miss Fortune"));

        // UTILITY는 SUPPORT와 동일하게 처리
        if (position.equals("UTILITY")) {
            position = "SUPPORT";
        }

        List<String> champions = defaultChampions.getOrDefault(position, defaultChampions.get("MIDDLE"));

        for (int i = 0; i < champions.size(); i++) {
            ChampionStats defaultStat = new ChampionStats(champions.get(i));
            defaultStat.setTotalGames(10);
            defaultStat.setWins(7);
            defaultStat.setAvgKDA(3.5);
            defaultStat.setAvgDamage(18000);
            defaultStat.setAvgVisionScore(25);
            defaultStat.setPickRate(0.3);
            defaultStat.setBanRate(0.2);
            result.add(defaultStat);
        }
    }

    @Data
    @AllArgsConstructor
    private static class ChampionStats {
        private String championName;
        private int totalGames;
        private int wins;
        private double avgKDA;
        private double avgDamage;
        private double avgVisionScore;
        private double pickRate;
        private double banRate; // 실제 밴 데이터는 없으므로 추정치로 계산

        public ChampionStats(String championName) {
            this.championName = championName;
            this.totalGames = 0;
            this.wins = 0;
            this.avgKDA = 0;
            this.avgDamage = 0;
            this.avgVisionScore = 0;
            this.pickRate = 0;
            this.banRate = 0;
        }

        // Setter 메서드 추가 (테스트/기본값 설정용)
        public void setTotalGames(int totalGames) {
            this.totalGames = totalGames;
        }

        public void setWins(int wins) {
            this.wins = wins;
        }

        public void setAvgKDA(double avgKDA) {
            this.avgKDA = avgKDA;
        }

        public void setAvgDamage(double avgDamage) {
            this.avgDamage = avgDamage;
        }

        public void setAvgVisionScore(double avgVisionScore) {
            this.avgVisionScore = avgVisionScore;
        }

        public void setPickRate(double pickRate) {
            this.pickRate = pickRate;
        }

        public void setBanRate(double banRate) {
            this.banRate = banRate;
        }

        public void addGame(ChallengerParticipantEntity participant) {
            totalGames++;
            if (participant.isWinner())
                wins++;

            double kda = participant.getDeaths() == 0 ? (participant.getKills() + participant.getAssists())
                    : (participant.getKills() + participant.getAssists()) / (double) participant.getDeaths();

            avgKDA = updateAverage(avgKDA, kda);
            avgDamage = updateAverage(avgDamage, participant.getTotalDamageDealtToChampions());
            avgVisionScore = updateAverage(avgVisionScore, participant.getVisionScore());
        }

        public void calculateRates(int totalGames) {
            this.pickRate = (double) this.totalGames / totalGames;

            // 인기도가 높은 챔피언일수록 밴률도 높다고 가정
            this.banRate = Math.min(0.8, this.pickRate * 1.5 * getWinRate());
        }

        private double updateAverage(double currentAvg, double newValue) {
            return (currentAvg * (totalGames - 1) + newValue) / totalGames;
        }

        public double getWinRate() {
            return wins / (double) totalGames;
        }

        public double getScore() {
            // 성능 점수 계산 (승률 40%, 픽률 30%, 밴률 10%, KDA 10%, 데미지 5%, 시야점수 5%)
            return getWinRate() * 0.4 +
                    pickRate * 0.3 +
                    banRate * 0.1 +
                    (avgKDA / 5.0) * 0.1 +
                    (avgDamage / 30000.0) * 0.05 +
                    (avgVisionScore / 50.0) * 0.05;
        }
    }
}