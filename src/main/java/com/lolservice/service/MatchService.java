package com.lolservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.lolservice.dto.MatchDTO;
import com.lolservice.dto.MatchDTO.ParticipantDTO;
import com.lolservice.dto.SummonerDTO;
import java.util.List;
import java.util.ArrayList;
import com.lolservice.dto.LeagueEntryDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.lolservice.dto.AccountDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.web.client.HttpClientErrorException;
import java.time.LocalDateTime;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZoneId;
import com.lolservice.entity.ChallengerParticipantEntity;
import org.springframework.data.redis.core.RedisTemplate;

import com.lolservice.repository.MatchRepository;
import com.lolservice.repository.ChallengerParticipantRepository;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    @Value("${riot.api.key}")
    private String apiKey;

    @Value("${riot.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, List<MatchDTO>> redisTemplate;
    private final MatchRepository matchRepository;
    private final ChallengerParticipantRepository challengerParticipantRepository;

    public List<MatchDTO> getMatchHistory(String puuid) {
        String cacheKey = "match_history:" + puuid;

        try {
            List<MatchDTO> cachedMatches = (List<MatchDTO>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedMatches != null && !cachedMatches.isEmpty()) {
                return cachedMatches;
            }

            List<String> matchIds = getMatchIdsFromRiotApi(puuid);
            List<MatchDTO> matches = new ArrayList<>();

            for (String matchId : matchIds) {
                if (matchRepository.existsByMatchId(matchId)) {
                    List<ChallengerParticipantEntity> participants = matchRepository.findByMatchId(matchId);
                    if (!participants.isEmpty()) {
                        matches.add(convertToDTO(participants.get(0)));
                        continue;
                    }
                }

                MatchDTO match = getMatchFromRiotApi(matchId);
                if (match != null) {
                    matches.add(match);
                    saveParticipants(match);
                }
            }

            if (!matches.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, matches);
                redisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
            }
            
            return matches;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch match history: " + e.getMessage());
        }
    }

    public LeagueEntryDTO getSummonerRank(String summonerId) {
        try {
            String url = String.format("https://kr.api.riotgames.com/lol/league/v4/entries/by-summoner/%s", summonerId);
            log.info("Requesting rank info from Riot API: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List<LeagueEntryDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<LeagueEntryDTO>>() {
                    });

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                LeagueEntryDTO rankInfo = response.getBody().stream()
                        .filter(entry -> "RANKED_SOLO_5x5".equals(entry.getQueueType()))
                        .findFirst()
                        .orElse(null);

                if (rankInfo != null) {
                    // tier와 rank 대문자로 통일
                    if (rankInfo.getTier() != null) {
                        rankInfo.setTier(rankInfo.getTier().toUpperCase());
                    } else {
                        rankInfo.setTier("UNRANKED");
                    }

                    if (rankInfo.getRank() != null) {
                        rankInfo.setRank(rankInfo.getRank().toUpperCase());
                    } else {
                        rankInfo.setRank("I");
                    }

                    log.info("Rank info found - Tier: {}, Rank: {}, LP: {}",
                            rankInfo.getTier(), rankInfo.getRank(), rankInfo.getLeaguePoints());
                }

                return rankInfo;
            }

            log.info("No rank data found for summoner ID: {}", summonerId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching rank data: {}", e.getMessage());
            return null;
        }
    }

    public SummonerDTO getSummonerInfo(String summonerName) {
        try {
            // Riot ID 형식 확인 (#이 있는 경우)
            if (summonerName.contains("#")) {
                String[] parts = summonerName.split("#");
                String gameName = parts[0];
                String tagLine = parts.length > 1 ? parts[1] : "KR1";

                // Account-V1 API로 puuid 조회
                String puuid = getPuuidFromRiotId(gameName, tagLine);

                // Summoner-V4 API로 상세 정보 조회
                SummonerDTO summoner = getSummonerByPuuid(puuid);

                // 이름 설정
                summoner.setName(gameName); // 여기서 이름 설정

                return summoner;
            } else {
                // 기존 방식 (소환사 이름으로 직접 조회)
                String url = String.format("%s/lol/summoner/v4/summoners/by-name/%s",
                        baseUrl,
                        URLEncoder.encode(summonerName, StandardCharsets.UTF_8));

                SummonerDTO summoner = getRequest(url, SummonerDTO.class);
                summoner.setName(summonerName); // 여기서도 이름 설정
                return summoner;
            }
        } catch (Exception e) {
            log.error("Error getting summoner info for {}: {}", summonerName, e.getMessage());
            throw new RuntimeException("Failed to get summoner info: " + e.getMessage());
        }
    }

    public MatchDTO getCurrentGame(String summonerId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 1. 먼저 summonerId로 소환사 정보를 가져와서 puuid를 얻음
            String summonerUrl = String.format(
                    "https://kr.api.riotgames.com/lol/summoner/v4/summoners/%s",
                    summonerId);

            ResponseEntity<SummonerDTO> summonerResponse = restTemplate.exchange(
                    summonerUrl,
                    HttpMethod.GET,
                    entity,
                    SummonerDTO.class);

            if (summonerResponse.getBody() == null || summonerResponse.getBody().getPuuid() == null) {
                return null;
            }

            // 2. puuid로 현재 게임 정보 요청
            String currentGameUrl = String.format(
                    "https://kr.api.riotgames.com/lol/spectator/v5/active-games/by-summoner/%s",
                    summonerResponse.getBody().getPuuid()); // summonerId 대신 puuid 사용
            try {
                ResponseEntity<String> rawResponse = restTemplate.exchange(
                        currentGameUrl,
                        HttpMethod.GET,
                        entity,
                        String.class);

                if (rawResponse.getStatusCode().is2xxSuccessful()) {
                    // JSON 파싱
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(rawResponse.getBody());

                    // MatchDTO로 변환할 객체 생성
                    MatchDTO matchDTO = new MatchDTO();

                    // metadata와 info 객체 초기화
                    matchDTO.setMetadata(new MatchDTO.MetadataDTO());
                    matchDTO.setInfo(new MatchDTO.InfoDTO());

                    // gameId를 matchId로 설정
                    if (rootNode.has("id")) {
                        matchDTO.getMetadata().setMatchId(rootNode.get("id").asText());
                    }

                    // 게임 정보 설정
                    if (rootNode.has("metadata")) {
                        JsonNode metadata = rootNode.get("metadata");
                        if (metadata != null && metadata.has("gameVersion")) {
                            matchDTO.getInfo().setGameVersion(metadata.get("gameVersion").asText());
                        }
                    }

                    // 게임 상태 정보 설정
                    if (rootNode.has("gameData")) {
                        JsonNode gameData = rootNode.get("gameData");
                        if (gameData != null) {

                            // 게임 타입 저장
                            if (gameData.has("gameType")) {
                                // InfoDTO에 저장 가능한 필드가 없으면 새로운 필드 추가 필요
                                // 여기서는 설명용 로그만 출력
                                log.info("Game Type: {}", gameData.get("gameType").asText());
                            }

                            // 게임 시작 시간
                            if (gameData.has("gameStartTime")) {
                                long gameStartTime = gameData.get("gameStartTime").asLong();
                                matchDTO.getInfo().setGameCreation(gameStartTime);
                            }

                            // 게임 길이
                            if (gameData.has("gameLength")) {
                                long gameLength = gameData.get("gameLength").asLong();
                                matchDTO.getInfo().setGameDuration(gameLength);
                            }

                            // 맵 ID
                            if (gameData.has("mapId")) {
                                // 임시로 다른 필드에 저장
                                log.info("Map ID: {}", gameData.get("mapId").asInt());
                            }
                        }
                    }

                    // 참가자 정보 설정
                    if (rootNode.has("participants")) {
                        JsonNode participantsNode = rootNode.get("participants");
                        if (participantsNode != null && participantsNode.isArray()) {
                            List<ParticipantDTO> participants = new ArrayList<>();

                            for (JsonNode participantNode : participantsNode) {
                                ParticipantDTO participant = new ParticipantDTO();

                                // 소환사 이름
                                if (participantNode.has("summonerName")) {
                                    participant.setSummonerName(participantNode.get("summonerName").asText());
                                }

                                // PUUID
                                if (participantNode.has("puuid")) {
                                    participant.setPuuid(participantNode.get("puuid").asText());
                                }

                                // 챔피언 정보
                                if (participantNode.has("championId")) {
                                    participant.setChampionId(participantNode.get("championId").asInt());
                                }

                                if (participantNode.has("championName")) {
                                    participant.setChampionName(participantNode.get("championName").asText());
                                }

                                // 팀 정보 (100=블루팀, 200=레드팀)
                                if (participantNode.has("teamId")) {
                                    int teamId = participantNode.get("teamId").asInt();
                                    participant.setTeamPosition(teamId == 100 ? "BLUE" : "RED");
                                }

                                participants.add(participant);
                            }

                            matchDTO.getInfo().setParticipants(participants);
                        }
                    }

                    log.info("성공적으로 현재 게임 정보를 가져왔습니다. 참가자 수: {}",
                            matchDTO.getInfo().getParticipants() != null ? matchDTO.getInfo().getParticipants().size()
                                    : 0);

                    return matchDTO;
                }
                return null;

            } catch (HttpClientErrorException e) {
                log.error("HTTP Error Status: {}", e.getStatusCode());
                log.error("HTTP Error Response: {}", e.getResponseBodyAsString());

                if (e.getStatusCode().value() == 404) {
                    log.info("Summoner is not in game");
                    return null;
                }
                throw e;
            }
        } catch (Exception e) {
            log.error("Error in getCurrentGame: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch current game info: " + e.getMessage());
        }
    }

    private String getPuuidFromRiotId(String gameName, String tagLine) {
        String url = String.format("https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s",
                URLEncoder.encode(gameName, StandardCharsets.UTF_8),
                URLEncoder.encode(tagLine, StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<AccountDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                AccountDTO.class);

        return response.getBody().getPuuid();
    }

    private List<String> getMatchIdsFromRiotApi(String puuid) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = String.format(
                "https://asia.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=20",
                puuid);

        ResponseEntity<List<String>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                });

        return response.getBody() != null ? response.getBody() : new ArrayList<>();
    }

    private MatchDTO getMatchFromRiotApi(String matchId) {
        try {
            String url = String.format("https://asia.api.riotgames.com/lol/match/v5/matches/%s", matchId);
            log.info("Fetching match data from Riot API: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getBody());

                // MatchDTO 생성
                MatchDTO matchDTO = new MatchDTO();

                // metadata 추출
                JsonNode metadataNode = rootNode.path("metadata");
                MatchDTO.MetadataDTO metadataDTO = new MatchDTO.MetadataDTO();
                metadataDTO.setMatchId(metadataNode.path("matchId").asText());

                List<String> participants = new ArrayList<>();
                JsonNode participantsArray = metadataNode.path("participants");
                for (JsonNode participant : participantsArray) {
                    participants.add(participant.asText());
                }
                metadataDTO.setParticipants(participants);
                matchDTO.setMetadata(metadataDTO);

                // info 추출
                JsonNode infoNode = rootNode.path("info");
                MatchDTO.InfoDTO infoDTO = new MatchDTO.InfoDTO();
                infoDTO.setGameCreation(infoNode.path("gameCreation").asLong());
                infoDTO.setGameDuration(infoNode.path("gameDuration").asLong());
                infoDTO.setGameVersion(infoNode.path("gameVersion").asText());
                infoDTO.setQueueId(infoNode.path("queueId").asInt());

                // participants 추출
                List<ParticipantDTO> participantDTOs = new ArrayList<>();
                JsonNode participantsNode = infoNode.path("participants");

                // 게임 시간 계산
                long gameDuration = infoNode.path("gameDuration").asLong();
                LocalDateTime gameCreation = Instant.ofEpochMilli(infoNode.path("gameCreation").asLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                for (JsonNode participantNode : participantsNode) {
                    ParticipantDTO participantDTO = convertParticipantFromJson(participantNode, gameDuration,
                            gameCreation);
                    participantDTOs.add(participantDTO);
                }
                infoDTO.setParticipants(participantDTOs);

                // teams 추출
                List<MatchDTO.TeamDTO> teamDTOs = new ArrayList<>();
                JsonNode teamsNode = infoNode.path("teams");
                for (JsonNode teamNode : teamsNode) {
                    MatchDTO.TeamDTO teamDTO = new MatchDTO.TeamDTO();
                    teamDTO.setTeamId(teamNode.path("teamId").asInt());

                    // bans 추출 (존재하는 경우에만)
                    JsonNode bansNode = teamNode.path("bans");
                    if (!bansNode.isMissingNode() && bansNode.isArray()) {
                        List<MatchDTO.BanDTO> banDTOs = new ArrayList<>();
                        for (JsonNode banNode : bansNode) {
                            MatchDTO.BanDTO banDTO = new MatchDTO.BanDTO();
                            banDTO.setChampionId(banNode.path("championId").asInt());
                            banDTO.setPickTurn(banNode.path("pickTurn").asInt());
                            banDTOs.add(banDTO);
                        }
                        teamDTO.setBans(banDTOs);
                    }

                    teamDTOs.add(teamDTO);
                }
                infoDTO.setTeams(teamDTOs);

                matchDTO.setInfo(infoDTO);

                return matchDTO;
            }
        } catch (Exception e) {
            log.error("Error fetching match data from Riot API: {}", e.getMessage());
        }
        return null;
    }

    private MatchDTO convertToDTO(ChallengerParticipantEntity entity) {
        MatchDTO dto = new MatchDTO();

        // metadata와 info 객체 초기화
        dto.setMetadata(new MatchDTO.MetadataDTO());
        dto.setInfo(new MatchDTO.InfoDTO());

        // 기본 정보 설정
        dto.getMetadata().setMatchId(entity.getMatchId());
        dto.getInfo().setGameCreation(entity.getGameCreation().toInstant(ZoneOffset.UTC).toEpochMilli());
        dto.getInfo().setGameDuration(entity.getGameDuration());
        dto.getInfo().setGameVersion(entity.getGameVersion());

        // participants 리스트 초기화
        dto.getInfo().setParticipants(new ArrayList<>());

        // 참가자 정보 변환
        MatchDTO.ParticipantDTO participantDTO = new MatchDTO.ParticipantDTO();
        participantDTO.setPuuid(entity.getPuuid());
        participantDTO.setSummonerName(entity.getSummonerName());
        participantDTO.setChampionId(Integer.parseInt(entity.getChampionId()));
        participantDTO.setChampionName(entity.getChampionName());
        participantDTO.setTeamPosition(entity.getPosition());
        participantDTO.setWin(entity.isWinner());

        // 추가 스탯 정보
        participantDTO.setKills(entity.getKills());
        participantDTO.setDeaths(entity.getDeaths());
        participantDTO.setAssists(entity.getAssists());
        participantDTO.setTotalDamageDealtToChampions(entity.getTotalDamageDealtToChampions());
        participantDTO.setVisionScore(entity.getVisionScore());
        participantDTO.setGoldEarned(entity.getGoldEarned());

        // 멀티킬 정보
        participantDTO.setDoubleKills(entity.getDoubleKills());
        participantDTO.setTripleKills(entity.getTripleKills());
        participantDTO.setQuadraKills(entity.getQuadraKills());
        participantDTO.setPentaKills(entity.getPentaKills());
        participantDTO.setNeutralMinionsKilled(entity.getNeutralMinionsKilled());
        participantDTO.setFirstBloodKill(entity.isFirstBloodKill());
        participantDTO.setTurretKills(entity.getTurretKills());

        // 아이템 정보
        participantDTO.setItem0(entity.getItem0());
        participantDTO.setItem1(entity.getItem1());
        participantDTO.setItem2(entity.getItem2());
        participantDTO.setItem3(entity.getItem3());
        participantDTO.setItem4(entity.getItem4());
        participantDTO.setItem5(entity.getItem5());
        participantDTO.setItem6(entity.getItem6());

        // 룬과 스펠 정보
        participantDTO.setPrimaryRune(entity.getPrimaryRune());
        participantDTO.setSecondaryRune(entity.getSecondaryRune());
        participantDTO.setSpell1(entity.getSpell1());
        participantDTO.setSpell2(entity.getSpell2());

        dto.getInfo().getParticipants().add(participantDTO);

        return dto;
    }

    private void saveParticipants(MatchDTO match) {
        match.getInfo().getParticipants().forEach(p -> {
            ChallengerParticipantEntity participant = convertParticipantToEntity(p);
            participant.setMatchId(match.getMetadata().getMatchId());
            participant.setGameCreation(LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(match.getInfo().getGameCreation()),
                    ZoneId.systemDefault()));
            participant.setGameDuration(match.getInfo().getGameDuration());
            participant.setGameVersion(match.getInfo().getGameVersion());
            participant.setQueueTypeFromId(match.getInfo().getQueueId());
            challengerParticipantRepository.save(participant);
        });
    }

    private ChallengerParticipantEntity convertParticipantToEntity(ParticipantDTO dto) {
        ChallengerParticipantEntity entity = new ChallengerParticipantEntity();

        // 기본 정보
        entity.setPuuid(dto.getPuuid());
        entity.setSummonerName(dto.getSummonerName());
        entity.setChampionName(dto.getChampionName());
        entity.setPosition(dto.getTeamPosition());
        entity.setWinner(dto.isWin());

        // 기본 스탯
        entity.setKills(dto.getKills());
        entity.setDeaths(dto.getDeaths());
        entity.setAssists(dto.getAssists());
        entity.setTotalDamageDealtToChampions(dto.getTotalDamageDealtToChampions());
        entity.setVisionScore(dto.getVisionScore());
        entity.setTotalMinionsKilled(dto.getTotalMinionsKilled());
        entity.setGoldEarned(dto.getGoldEarned());

        // 아이템 정보
        entity.setItem0(dto.getItem0());
        entity.setItem1(dto.getItem1());
        entity.setItem2(dto.getItem2());
        entity.setItem3(dto.getItem3());
        entity.setItem4(dto.getItem4());
        entity.setItem5(dto.getItem5());
        entity.setItem6(dto.getItem6());

        // 룬과 스펠 정보
        entity.setPrimaryRune(dto.getPrimaryRune());
        entity.setSecondaryRune(dto.getSecondaryRune());
        entity.setSpell1(dto.getSpell1());
        entity.setSpell2(dto.getSpell2());

        // 게임 시간 정보
        entity.setGameDuration(dto.getGameDuration());
        entity.setGameCreation(dto.getGameCreation());

        return entity;
    }

    private SummonerDTO getSummonerByPuuid(String puuid) {
        String url = String.format("https://kr.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s", puuid);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<SummonerDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                SummonerDTO.class);

        return response.getBody();
    }

    private <T> T getRequest(String url, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<T> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                responseType);

        return response.getBody();
    }

    public List<MatchDTO> refreshMatchHistory(String summonerName) {
        // 캐시 무시하고 새로운 데이터 가져오기 로직
        log.info("Refreshing match history for summoner: {}", summonerName);

        // 1. 소환사 정보 조회
        SummonerDTO summoner = getSummonerInfo(summonerName);
        if (summoner == null) {
            throw new RuntimeException("Summoner not found: " + summonerName);
        }

        // 2. 캐시에서 해당 소환사의 매치 히스토리 삭제
        String cacheKey = "match_history:" + summoner.getPuuid();
        redisTemplate.delete(cacheKey);
        log.info("Deleted match history cache for summoner: {}", summonerName);

        return getMatchHistory(summoner.getPuuid()); // 새로운 데이터 조회
    }

    // API 응답에서 받은 매치 정보를 DTO로 변환하는 메서드
    private ParticipantDTO convertParticipantFromJson(JsonNode participantJson, long gameDuration,
            LocalDateTime gameCreation) {
        ParticipantDTO participantDTO = new ParticipantDTO();

        try {
            // 기본 정보
            participantDTO.setPuuid(participantJson.path("puuid").asText());
            participantDTO.setSummonerName(participantJson.path("summonerName").asText());
            participantDTO.setChampionId(participantJson.path("championId").asInt());
            participantDTO.setChampionName(participantJson.path("championName").asText());
            participantDTO.setTeamPosition(participantJson.path("teamPosition").asText());
            participantDTO.setWin(participantJson.path("win").asBoolean());

            // 스펠 정보 (Riot API 응답에서는 summoner1Id, summoner2Id로 옴)
            participantDTO.setSpell1(participantJson.path("summoner1Id").asInt());
            participantDTO.setSpell2(participantJson.path("summoner2Id").asInt());

            // 룬 정보
            JsonNode perks = participantJson.path("perks");
            if (!perks.isMissingNode()) {
                JsonNode styles = perks.path("styles");
                if (styles.isArray() && styles.size() > 0) {
                    // 주 룬 스타일 (첫 번째 스타일)
                    JsonNode primaryStyle = styles.get(0);
                    if (!primaryStyle.isMissingNode()) {
                        JsonNode selections = primaryStyle.path("selections");
                        if (selections.isArray() && selections.size() > 0) {
                            // 메인 키스톤 룬 (첫 번째 선택)
                            participantDTO.setPrimaryRune(selections.get(0).path("perk").asInt());
                        }
                        // 주 룬 스타일 ID
                        participantDTO.setSecondaryRune(primaryStyle.path("style").asInt());
                    }

                    // 부 룬 스타일 (두 번째 스타일)
                    if (styles.size() > 1) {
                        JsonNode secondaryStyle = styles.get(1);
                        if (!secondaryStyle.isMissingNode()) {
                            participantDTO.setSecondaryRune(secondaryStyle.path("style").asInt());
                        }
                    }
                }
            }

            // KDA 정보
            participantDTO.setKills(participantJson.path("kills").asInt());
            participantDTO.setDeaths(participantJson.path("deaths").asInt());
            participantDTO.setAssists(participantJson.path("assists").asInt());
            participantDTO.setTotalDamageDealtToChampions(participantJson.path("totalDamageDealtToChampions").asInt());
            participantDTO.setVisionScore(participantJson.path("visionScore").asInt());
            participantDTO.setGoldEarned(participantJson.path("goldEarned").asInt());

            // 멀티킬 정보
            participantDTO.setDoubleKills(participantJson.path("doubleKills").asInt());
            participantDTO.setTripleKills(participantJson.path("tripleKills").asInt());
            participantDTO.setQuadraKills(participantJson.path("quadraKills").asInt());
            participantDTO.setPentaKills(participantJson.path("pentaKills").asInt());
            participantDTO.setFirstBloodKill(participantJson.path("firstBloodKill").asBoolean());
            participantDTO.setTurretKills(participantJson.path("turretKills").asInt());
            participantDTO.setTotalMinionsKilled(participantJson.path("totalMinionsKilled").asInt());
            participantDTO.setNeutralMinionsKilled(participantJson.path("neutralMinionsKilled").asInt());

            // 아이템 정보
            participantDTO.setItem0(participantJson.path("item0").asInt());
            participantDTO.setItem1(participantJson.path("item1").asInt());
            participantDTO.setItem2(participantJson.path("item2").asInt());
            participantDTO.setItem3(participantJson.path("item3").asInt());
            participantDTO.setItem4(participantJson.path("item4").asInt());
            participantDTO.setItem5(participantJson.path("item5").asInt());
            participantDTO.setItem6(participantJson.path("item6").asInt());

            // 게임 관련 정보
            participantDTO.setGameDuration(gameDuration);
            participantDTO.setGameCreation(gameCreation);

            log.debug("Successfully converted participant: {}", participantDTO.getSummonerName());
        } catch (Exception e) {
            log.error("Error converting participant JSON: {}", e.getMessage());
        }

        return participantDTO;
    }
}