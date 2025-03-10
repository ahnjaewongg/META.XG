package com.lolservice.service;

import com.lolservice.entity.MatchParticipantEntity;
import com.lolservice.repository.MatchParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentalAnalysisService {

    private final MatchParticipantRepository matchParticipantRepository;
    private final RiotApiService riotApiService;

    // 틸트 감지를 위한 임계값
    private static final int MIN_DEATH_THRESHOLD = 5;
    private static final double HIGH_DEATH_RATIO = 0.25;
    private static final int MIN_GAMES_FOR_ANALYSIS = 5;

    /**
     * 소환사의 정신 상태를 분석하고 추천사항을 제공
     *
     * @param summonerName 소환사 이름
     * @return 분석 결과 (연패 정보, 부정적 패턴, 틸트 확률, 추천사항)
     */
    public Map<String, Object> analyzeMentalState(String summonerName) {
        log.info("Analyzing mental for summoner: {}", summonerName);
        Map<String, Object> result = new HashMap<>();

        try {
            // 소환사 이름에서 공백을 제거하거나 추가하여 다양한 검색 패턴 생성
            String normalizedName = summonerName.trim();
            String noSpaceName = normalizedName.replaceAll("\\s+", "");

            // 1. 먼저 데이터베이스에서 다양한 방법으로 소환사를 검색
            List<MatchParticipantEntity> participants = searchSummonerInDb(normalizedName, noSpaceName);

            // 2. 데이터베이스에서 찾지 못한 경우 Riot API를 통해 소환사 정보 검색
            if (participants.isEmpty()) {
                log.info("Summoner not found in database, trying Riot API: {}", summonerName);
                try {
                    // Riot API에서 소환사 정보를 검색
                    String puuid = getPuuidFromRiotApi(normalizedName);
                    if (puuid != null) {
                        log.info("Found summoner in Riot API with PUUID: {}", puuid);
                        // PUUID로 데이터베이스에서 재검색
                        participants = matchParticipantRepository.findByPuuid(puuid);

                        if (participants.isEmpty()) {
                            log.info("No match data found for summoner with PUUID: {}", puuid);
                            // 기본 응답을 생성하고 실제 소환사 이름 추가
                            return createDefaultResponse(normalizedName, true);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error fetching summoner data from Riot API", e);
                }

                if (participants.isEmpty()) {
                    log.warn("Summoner not found in database or Riot API: {}", summonerName);
                    result.put("error", "소환사를 찾을 수 없습니다: " + summonerName);
                    result.put("suggestedSearches", getSuggestedSummonerNames());
                    return result;
                }
            }

            // 찾은 소환사 데이터 정렬 (최신 게임 순)
            participants.sort(Comparator.comparing(MatchParticipantEntity::getCreatedAt).reversed());

            // 실제 소환사 이름 가져오기
            String foundSummonerName = participants.get(0).getSummonerName();
            log.info("Found summoner: {} (searched as: {})", foundSummonerName, summonerName);

            // 최소 분석 가능한 게임 수 확인
            if (participants.size() < MIN_GAMES_FOR_ANALYSIS) {
                log.info("Not enough games for analysis. Found {} games, minimum required: {}",
                        participants.size(), MIN_GAMES_FOR_ANALYSIS);
                return createDefaultResponse(foundSummonerName, true);
            }

            // 3. 실제 멘탈 상태 분석 수행
            return performMentalAnalysis(participants, foundSummonerName);

        } catch (Exception e) {
            log.error("Error analyzing mental state", e);
            result.put("error", "분석 중 오류 발생: " + e.getMessage());
            return result;
        }
    }

    /**
     * 데이터베이스에서 다양한 방법으로 소환사를 검색
     */
    private List<MatchParticipantEntity> searchSummonerInDb(String normalizedName, String noSpaceName) {
        // 1. 정확한 이름으로 검색
        List<MatchParticipantEntity> participants = matchParticipantRepository
                .findBySummonerNameIgnoreCase(normalizedName);
        if (!participants.isEmpty()) {
            log.info("Found summoner with exact name: {}", normalizedName);
            return participants;
        }

        // 2. 공백 제거한 이름으로 검색
        if (!normalizedName.equals(noSpaceName)) {
            log.info("Trying to search without spaces: {}", noSpaceName);
            participants = matchParticipantRepository.findBySummonerNameIgnoreCase(noSpaceName);
            if (!participants.isEmpty()) {
                log.info("Found summoner without spaces: {}", noSpaceName);
                return participants;
            }
        }

        // 3. 공백이 없는 경우 공백 추가해서 검색
        // GT63 → G T 6 3 형식이 아닌 AMG GT63 OWNER 형식으로 공백 추가
        if (normalizedName.equals(noSpaceName)) {
            // 가능한 공백 위치 패턴 (앞글자/숫자 변경 지점)
            List<String> possiblePatterns = generatePossibleNamePatterns(noSpaceName);
            for (String pattern : possiblePatterns) {
                log.info("Trying pattern with spaces: {}", pattern);
                participants = matchParticipantRepository.findBySummonerNameIgnoreCase(pattern);
                if (!participants.isEmpty()) {
                    log.info("Found summoner with pattern: {}", pattern);
                    return participants;
                }
            }
        }

        // 4. 부분 일치 검색 (태그 제외)
        if (normalizedName.contains("#")) {
            String nameWithoutTag = normalizedName.substring(0, normalizedName.indexOf("#")).trim();
            log.info("Trying to search without tag: {}", nameWithoutTag);

            // 모든 참가자를 가져와서 부분 일치 검색
            List<MatchParticipantEntity> allParticipants = matchParticipantRepository.findAll();
            participants = allParticipants.stream()
                    .filter(p -> p.getSummonerName() != null &&
                            p.getSummonerName().toLowerCase().contains(nameWithoutTag.toLowerCase()))
                    .collect(Collectors.toList());

            if (!participants.isEmpty()) {
                log.info("Found summoner with partial match: {}", participants.get(0).getSummonerName());
                return participants;
            }
        }

        return new ArrayList<>();
    }

    /**
     * 공백이 없는 이름에 가능한 공백 패턴 생성
     */
    private List<String> generatePossibleNamePatterns(String name) {
        List<String> patterns = new ArrayList<>();

        // 문자에서 숫자로 변경되는 지점에 공백 추가
        StringBuilder pattern = new StringBuilder();
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            pattern.append(chars[i]);
            if (i < chars.length - 1) {
                if (Character.isLetter(chars[i]) && Character.isDigit(chars[i + 1]) ||
                        Character.isDigit(chars[i]) && Character.isLetter(chars[i + 1])) {
                    pattern.append(" ");
                }
            }
        }
        patterns.add(pattern.toString());

        // 대문자가 연속으로 나오는 경우 사이에 공백 추가
        pattern = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            pattern.append(chars[i]);
            if (i < chars.length - 1) {
                if (Character.isUpperCase(chars[i]) && Character.isUpperCase(chars[i + 1]) &&
                        i > 0 && !Character.isWhitespace(chars[i - 1])) {
                    pattern.append(" ");
                }
            }
        }
        patterns.add(pattern.toString());

        // 특수한 경우: "AMGGT63OWNER" → "AMG GT63 OWNER"
        if (name.toUpperCase().contains("AMG") && name.toUpperCase().contains("GT")) {
            String processed = name.replaceAll("(?i)AMG", "AMG ");
            processed = processed.replaceAll("(?i)GT(\\d+)", "GT$1 ");
            processed = processed.trim();
            patterns.add(processed);

            // 인위적으로 "AMG GT63 OWNER" 패턴 추가
            if (processed.toUpperCase().contains("OWNER")) {
                processed = processed.replaceAll("(?i)GT(\\d+)\\s+", "GT$1 ");
                patterns.add(processed);
            }
        }

        return patterns.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Riot API에서 소환사의 PUUID 가져오기
     */
    private String getPuuidFromRiotApi(String summonerName) {
        try {
            // 먼저 태그가 있는지 확인
            if (summonerName.contains("#")) {
                // Riot ID API 사용
                return riotApiService.getRiotIdByNameAndTag(summonerName);
            } else {
                // 기존 소환사 이름 API 사용
                return riotApiService.getSummonerByName(summonerName).getPuuid();
            }
        } catch (Exception e) {
            log.error("Error getting PUUID from Riot API for summoner: {}", summonerName, e);
            return null;
        }
    }

    /**
     * 기본 응답 생성
     */
    private Map<String, Object> createDefaultResponse(String summonerName, boolean fromRiotApi) {
        Map<String, Object> result = new HashMap<>();

        // 연패 정보
        Map<String, Object> losingStreak = new HashMap<>();
        losingStreak.put("currentLosingStreak", 0);
        losingStreak.put("maxLosingStreak", 0);
        losingStreak.put("inLosingStreak", false);
        losingStreak.put("message", "현재 연패중이 아닙니다.");

        // 부정적 패턴 및 틸트 확률
        List<Map<String, String>> negativePatterns = new ArrayList<>();
        double tiltProbability = 0.0;

        // 추천사항
        List<Map<String, String>> recommendations = new ArrayList<>();
        Map<String, String> recommendation = new HashMap<>();
        recommendation.put("type", "general");
        recommendation.put("message", "긍정적인 마인드셋을 유지하세요.");

        if (fromRiotApi) {
            recommendation.put("explanation", "소환사 정보는 확인되었으나 게임 기록이 분석에 필요한 최소 수량에 미치지 못합니다.");
        } else {
            recommendation.put("explanation", "멘탈 분석 기능은 현재 개발 중입니다.");
        }

        recommendations.add(recommendation);

        // 결과 맵에 추가
        result.put("losingStreak", losingStreak);
        result.put("negativePatterns", negativePatterns);
        result.put("tiltProbability", tiltProbability);
        result.put("recommendations", recommendations);
        result.put("foundSummonerName", summonerName);

        log.info("Created default mental analysis response for summoner: {}", summonerName);
        return result;
    }

    /**
     * 추천 검색어 목록 반환
     */
    private List<String> getSuggestedSummonerNames() {
        // 최근에 검색된 인기 소환사 목록을 반환
        return matchParticipantRepository.findAll().stream()
                .filter(p -> p.getSummonerName() != null)
                .map(MatchParticipantEntity::getSummonerName)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 실제 멘탈 상태 분석을 수행하는 메서드
     */
    private Map<String, Object> performMentalAnalysis(List<MatchParticipantEntity> participants, String summonerName) {
        log.info("Performing mental analysis for {} games", participants.size());
        Map<String, Object> result = new HashMap<>();

        // 데이터 준비
        List<MatchParticipantEntity> recentGames = participants.stream()
                .limit(20) // 최근 20게임만 분석
                .collect(Collectors.toList());

        // 연패 분석
        Map<String, Object> losingStreak = analyzeLosingStreak(recentGames);

        // 부정적 패턴 분석
        List<Map<String, String>> negativePatterns = analyzeNegativePatterns(recentGames);

        // 틸트 확률 계산
        double tiltProbability = calculateTiltProbability(recentGames,
                (int) losingStreak.get("currentLosingStreak"),
                (boolean) losingStreak.get("inLosingStreak"));

        // 멘탈 케어 추천사항 생성
        List<Map<String, String>> recommendations = generateRecommendations(
                tiltProbability, negativePatterns, losingStreak);

        // 결과 맵에 추가
        result.put("losingStreak", losingStreak);
        result.put("negativePatterns", negativePatterns);
        result.put("tiltProbability", tiltProbability);
        result.put("recommendations", recommendations);
        result.put("foundSummonerName", summonerName);

        log.info("Mental analysis completed for summoner: {}", summonerName);
        return result;
    }

    /**
     * 연패 상태를 분석
     */
    private Map<String, Object> analyzeLosingStreak(List<MatchParticipantEntity> games) {
        Map<String, Object> result = new HashMap<>();

        int currentStreak = 0;
        int maxStreak = 0;
        boolean inLosingStreak = false;

        // 현재 연패 상태 확인 (연속된 패배)
        for (MatchParticipantEntity game : games) {
            if (!game.isWin()) {
                currentStreak++;
            } else {
                break;
            }
        }

        // 최대 연패 기록 계산
        int tempStreak = 0;
        for (MatchParticipantEntity game : games) {
            if (!game.isWin()) {
                tempStreak++;
            } else {
                maxStreak = Math.max(maxStreak, tempStreak);
                tempStreak = 0;
            }
        }
        // 마지막 게임들이 모두 패배일 경우 확인
        maxStreak = Math.max(maxStreak, tempStreak);

        // 3연패 이상일 경우 연패 중으로 판단
        inLosingStreak = currentStreak >= 3;

        String message;
        if (inLosingStreak) {
            message = String.format("현재 %d연패 중입니다. 잠시 휴식이 필요할 수 있습니다.", currentStreak);
        } else if (currentStreak > 0) {
            message = String.format("현재 %d연패 중이지만, 아직 틸트 상태는 아닙니다.", currentStreak);
        } else {
            message = "현재 연패중이 아닙니다.";
        }

        result.put("currentLosingStreak", currentStreak);
        result.put("maxLosingStreak", maxStreak);
        result.put("inLosingStreak", inLosingStreak);
        result.put("message", message);

        return result;
    }

    /**
     * 부정적인 게임 패턴 분석
     */
    private List<Map<String, String>> analyzeNegativePatterns(List<MatchParticipantEntity> games) {
        List<Map<String, String>> patterns = new ArrayList<>();

        // 높은 데스 비율 확인
        int highDeathGames = 0;
        for (MatchParticipantEntity game : games) {
            if (game.getDeaths() >= MIN_DEATH_THRESHOLD &&
                    (double) game.getDeaths()
                            / (game.getKills() + game.getDeaths() + game.getAssists()) > HIGH_DEATH_RATIO) {
                highDeathGames++;
            }
        }

        // 전체 게임의 30% 이상이 높은 데스 비율일 경우
        if ((double) highDeathGames / games.size() > 0.3) {
            Map<String, String> pattern = new HashMap<>();
            pattern.put("type", "high_death");
            pattern.put("message", "사망 횟수가 높은 경향이 있습니다. 포지셔닝과 상황 판단에 주의하세요.");
            patterns.add(pattern);
        }

        // 낮은 CS 확인 (정글러 제외)
        int lowCsGames = 0;
        for (MatchParticipantEntity game : games) {
            if (!game.getTeamPosition().equalsIgnoreCase("JUNGLE") &&
                    game.getTotalMinionsKilled() < (game.getGameDuration() / 60) * 5) {
                lowCsGames++;
            }
        }

        // 전체 게임의 40% 이상이 낮은 CS일 경우 (정글러 제외)
        long nonJungleGames = games.stream().filter(g -> !g.getTeamPosition().equalsIgnoreCase("JUNGLE")).count();
        if (nonJungleGames > 0 && (double) lowCsGames / nonJungleGames > 0.4) {
            Map<String, String> pattern = new HashMap<>();
            pattern.put("type", "low_cs");
            pattern.put("message", "CS 수급이 부족한 경향이 있습니다. 마지막 타격에 집중하세요.");
            patterns.add(pattern);
        }

        // 시야 점수 확인 (미드, 정글, 서포터)
        int lowVisionGames = 0;
        for (MatchParticipantEntity game : games) {
            String position = game.getTeamPosition().toUpperCase();
            int expectedVision = 0;

            switch (position) {
                case "SUPPORT":
                    expectedVision = (game.getGameDuration() / 60) * 2;
                    break;
                case "JUNGLE":
                case "MID":
                    expectedVision = (game.getGameDuration() / 60);
                    break;
                default:
                    expectedVision = (game.getGameDuration() / 120);
            }

            if (game.getVisionScore() < expectedVision) {
                lowVisionGames++;
            }
        }

        // 전체 게임의 50% 이상이 낮은 시야 점수일 경우
        if ((double) lowVisionGames / games.size() > 0.5) {
            Map<String, String> pattern = new HashMap<>();
            pattern.put("type", "low_vision");
            pattern.put("message", "시야 확보가 부족합니다. 와드 사용에 더 신경 쓰세요.");
            patterns.add(pattern);
        }

        return patterns;
    }

    /**
     * 틸트 확률 계산
     */
    private double calculateTiltProbability(List<MatchParticipantEntity> games, int currentLosingStreak,
            boolean inLosingStreak) {
        // 기본 확률
        double probability = 0.0;

        // 1. 연패 요소 (최대 0.5)
        if (inLosingStreak) {
            probability += Math.min(0.5, currentLosingStreak * 0.1);
        }

        // 2. 최근 게임 성적 요소 (최대 0.3)
        int recentGamesCount = Math.min(5, games.size());
        int losses = 0;

        for (int i = 0; i < recentGamesCount; i++) {
            if (!games.get(i).isWin()) {
                losses++;
            }
        }

        probability += (double) losses / recentGamesCount * 0.3;

        // 3. KDA 요소 (최대 0.2)
        double kdaFactor = 0.0;
        for (int i = 0; i < recentGamesCount; i++) {
            MatchParticipantEntity game = games.get(i);
            double kda = game.getDeaths() == 0 ? 5.0
                    : (double) (game.getKills() + game.getAssists()) / game.getDeaths();

            if (kda < 1.0) {
                kdaFactor += 0.04;
            } else if (kda < 2.0) {
                kdaFactor += 0.02;
            }
        }

        probability += kdaFactor;

        return Math.min(1.0, probability);
    }

    /**
     * 멘탈 케어 추천사항 생성
     */
    private List<Map<String, String>> generateRecommendations(double tiltProbability,
            List<Map<String, String>> negativePatterns, Map<String, Object> losingStreak) {

        List<Map<String, String>> recommendations = new ArrayList<>();

        // 1. 틸트 확률에 따른 추천
        if (tiltProbability > 0.7) {
            Map<String, String> rec = new HashMap<>();
            rec.put("type", "break");
            rec.put("message", "게임을 잠시 쉬는 것을 추천합니다.");
            rec.put("explanation", "틸트 확률이 높습니다. 1-2시간 휴식 후 돌아오세요.");
            recommendations.add(rec);
        } else if (tiltProbability > 0.4) {
            Map<String, String> rec = new HashMap<>();
            rec.put("type", "mindset");
            rec.put("message", "마인드셋 재조정이 필요합니다.");
            rec.put("explanation", "긍정적인 마인드로 게임을 진행하세요. 팀원들과의 소통에 집중하세요.");
            recommendations.add(rec);
        }

        // 2. 연패 상태에 따른 추천
        boolean inLosingStreak = (boolean) losingStreak.get("inLosingStreak");
        if (inLosingStreak) {
            Map<String, String> rec = new HashMap<>();
            rec.put("type", "strategy");
            rec.put("message", "플레이 스타일을 변경해보세요.");
            rec.put("explanation", "연패 중일 때는 안전한 플레이와 기본기에 집중하는 것이 좋습니다.");
            recommendations.add(rec);
        }

        // 3. 부정적 패턴에 따른 추천
        for (Map<String, String> pattern : negativePatterns) {
            String type = pattern.get("type");

            Map<String, String> rec = new HashMap<>();
            switch (type) {
                case "high_death":
                    rec.put("type", "positioning");
                    rec.put("message", "포지셔닝 개선에 집중하세요.");
                    rec.put("explanation", "사망이 많은 경우, 미니맵을 더 자주 확인하고 적의 위치를 예측하세요.");
                    break;
                case "low_cs":
                    rec.put("type", "farming");
                    rec.put("message", "CS 연습이 필요합니다.");
                    rec.put("explanation", "연습 모드에서 10분에 70CS를 목표로 훈련해보세요.");
                    break;
                case "low_vision":
                    rec.put("type", "vision");
                    rec.put("message", "시야 확보를 개선하세요.");
                    rec.put("explanation", "핑크 와드를 적극 활용하고, 컨트롤 와드 위치를 전략적으로 선택하세요.");
                    break;
                default:
                    continue;
            }
            recommendations.add(rec);
        }

        // 추천사항이 없는 경우 기본 추천
        if (recommendations.isEmpty()) {
            Map<String, String> rec = new HashMap<>();
            rec.put("type", "general");
            rec.put("message", "현재 멘탈 상태가 양호합니다.");
            rec.put("explanation", "지금처럼 긍정적인 마인드셋을 유지하세요.");
            recommendations.add(rec);
        }

        return recommendations;
    }

}