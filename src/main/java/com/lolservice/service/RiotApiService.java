package com.lolservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

import com.lolservice.dto.LeagueListDTO;
import com.lolservice.dto.LeagueEntryDTO;
import com.lolservice.dto.MatchDTO;
import com.lolservice.exception.RiotApiException;
import com.lolservice.dto.SummonerDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lolservice.dto.RiotAccountDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.lolservice.util.WebUtils;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RiotApiService {
    @Value("${riot.api.key}")
    private String apiKey;

    private final String KR_API_URL = "https://kr.api.riotgames.com";
    private final String ASIA_API_URL = "https://asia.api.riotgames.com";
    private final RestTemplate restTemplate = new RestTemplate();
    private final Cache<String, List<LeagueEntryDTO>> leagueCache;
    private static final int CACHE_DURATION_MINUTES = 5;

    public RiotApiService() {
        this.leagueCache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_DURATION_MINUTES, TimeUnit.MINUTES)
                .build();
    }

    public List<String> getMatchList(String puuid, int count) {
        try {
            long startTime = System.currentTimeMillis() / 1000 - (7 * 24 * 60 * 60);

            String url = String.format("%s/lol/match/v5/matches/by-puuid/%s/ids?startTime=%d&queue=420&count=%d",
                    ASIA_API_URL, puuid, startTime, count);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String[].class);

            if (response.getBody() != null) {
                List<String> matchIds = Arrays.asList(response.getBody());
                if (matchIds.isEmpty()) {
                    log.warn("Empty response body from API: {}", response);
                }
                return matchIds;
            }

            log.warn("No matches found for puuid {}, response: {}", puuid, response);
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("Error getting match list for puuid {}: {}", puuid, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public MatchDTO getMatch(String matchId) {
        String url = ASIA_API_URL + "/lol/match/v5/matches/" + matchId;
        int maxRetries = 3;
        int retryCount = 0;
        long waitTime = 2000;

        while (retryCount < maxRetries) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Riot-Token", apiKey);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class);

                ObjectMapper mapper = new ObjectMapper();
                MatchDTO matchDTO = mapper.readValue(response.getBody(), MatchDTO.class);

                // 필요한 정보만 로깅
                log.info("Match {} loaded - Queue: {}, Duration: {}min, Participants: {}",
                        matchId,
                        matchDTO.getInfo().getQueueId(),
                        matchDTO.getInfo().getGameDuration() / 60,
                        matchDTO.getInfo().getParticipants().size());

                return matchDTO;

            } catch (Exception e) {
                log.error("Error fetching match data for {}: {}", matchId, e.getMessage());
                retryCount++;
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(waitTime);
                        waitTime *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }

        // 모든 재시도 실패 후
        log.error("Failed to fetch match {} after {} retries", matchId, maxRetries);
        return null;
    }

    public String getPuuid(String summonerId) {
        String url = KR_API_URL + "/lol/summoner/v4/summoners/" + summonerId;
        try {
            var summoner = getRequest(url, SummonerDTO.class);
            return summoner.getPuuid();
        } catch (Exception e) {
            throw e;
        }
    }

    private <T> T getRequest(String url, Class<T> responseType) throws RiotApiException {
        int maxRetries = 3;
        int retryCount = 0;
        long waitTime = 2000; // 2초로 변경

        while (retryCount < maxRetries) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Riot-Token", apiKey);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class);

                if (response.getBody() == null) {
                    throw new RiotApiException("Empty response body from URL: " + url);
                }

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.getBody(), responseType);
            } catch (Exception e) {
                log.error("Error in request to {}: {}", url, e.getMessage());
                if (e.getMessage().contains("429")) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        log.warn("Rate limit exceeded (attempt {}/{}), waiting {} seconds...",
                                retryCount, maxRetries, waitTime / 1000);
                        try {
                            Thread.sleep(waitTime);
                            waitTime *= 2;
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RiotApiException("Request interrupted");
                        }
                    } else {
                        throw new RiotApiException("Rate limit exceeded after " + maxRetries + " retries");
                    }
                } else {
                    throw new RiotApiException(e.getMessage());
                }
            }
        }
        throw new RiotApiException("Max retries exceeded");
    }

    public List<LeagueEntryDTO> getChallengerLeague() {
        return leagueCache.get("challenger", k -> {
            try {
                String url = KR_API_URL + "/lol/league/v4/challengerleagues/by-queue/RANKED_SOLO_5x5";
                LeagueListDTO leagueList = getRequest(url, LeagueListDTO.class);

                if (leagueList != null && leagueList.getEntries() != null) {
                    List<LeagueEntryDTO> entries = leagueList.getEntries();

                    // 기본 정보 설정
                    entries.forEach(entry -> {
                        entry.setTier("CHALLENGER");
                        entry.setQueueType("RANKED_SOLO_5x5");
                    });

                    // LP 기준으로 정렬
                    entries.sort((a, b) -> b.getLeaguePoints() - a.getLeaguePoints());

                    // 상위 10명의 Riot ID 조회
                    for (int i = 0; i < Math.min(10, entries.size()); i++) {
                        LeagueEntryDTO entry = entries.get(i);
                        try {
                            // 1. PUUID 조회
                            String summonerUrl = KR_API_URL + "/lol/summoner/v4/summoners/" + entry.getSummonerId();
                            SummonerDTO summoner = getRequest(summonerUrl, SummonerDTO.class);

                            if (summoner != null) {
                                // 2. Riot ID 조회
                                String riotAccountUrl = ASIA_API_URL + "/riot/account/v1/accounts/by-puuid/"
                                        + summoner.getPuuid();
                                RiotAccountDTO account = getRequest(riotAccountUrl, RiotAccountDTO.class);

                                if (account != null) {
                                    entry.setSummonerName(account.getGameName() + "#" + account.getTagLine());
                                }
                                Thread.sleep(1500); // Rate limit 방지
                            }
                        } catch (Exception e) {
                            log.error("Error fetching Riot ID for LP {}: {}", entry.getLeaguePoints(), e.getMessage());
                            entry.setSummonerName("Challenger #" + entry.getLeaguePoints());
                        }
                    }

                    // 나머지는 임시 이름 설정
                    for (int i = 10; i < entries.size(); i++) {
                        entries.get(i).setSummonerName("Challenger #" + entries.get(i).getLeaguePoints());
                    }

                    return entries;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                log.error("Error fetching Challenger league: {}", e.getMessage());
                return new ArrayList<>();
            }
        });
    }

    public List<String> getChallengerPlayerIds() {
        return getChallengerLeague().stream()
                .map(LeagueEntryDTO::getSummonerId)
                .toList();
    }

    public String getRiotId(String puuid) {
        String url = ASIA_API_URL + "/riot/account/v1/accounts/by-puuid/" + puuid;
        try {
            RiotAccountDTO account = getRequest(url, RiotAccountDTO.class);
            if (account != null && account.getGameName() != null) {
                String riotId = account.getGameName() + "#" + account.getTagLine();
                log.info("Retrieved Riot ID {} for PUUID {}", riotId, puuid);
                return riotId;
            }
        } catch (Exception e) {
            log.error("Error fetching Riot ID for puuid {}: {}", puuid, e.getMessage());
        }
        return null;
    }

    public SummonerDTO getSummonerByName(String summonerName) {

        String[] parts = summonerName.split("#");
        if (parts.length != 2) {
            throw new RiotApiException("Invalid Riot ID format. Expected format: 'name#tag'");
        }
        String gameName = parts[0];
        String tagLine = parts[1];

        try {
            // 2. PUUID 조회
            String accountUrl = ASIA_API_URL + "/riot/account/v1/accounts/by-riot-id/" +
                    WebUtils.encodeURIComponent(gameName) + "/" +
                    WebUtils.encodeURIComponent(tagLine);

            RiotAccountDTO account = getRequest(accountUrl, RiotAccountDTO.class);

            if (account == null || account.getPuuid() == null) {
                throw new RiotApiException("Failed to get PUUID for Riot ID: " + summonerName);
            }

            // 3. PUUID로 소환사 정보 조회
            String summonerUrl = KR_API_URL + "/lol/summoner/v4/summoners/by-puuid/" + account.getPuuid();
            SummonerDTO summoner = getRequest(summonerUrl, SummonerDTO.class);

            if (summoner == null) {
                throw new RiotApiException("Failed to get Summoner data for PUUID: " + account.getPuuid());
            }

            return summoner;
        } catch (Exception e) {
            log.error("Error fetching summoner data: {}", e.getMessage(), e);
            throw new RiotApiException("Failed to fetch summoner data: " + e.getMessage());
        }
    }

    public List<String> getMatchIdsByPuuid(String puuid) {
        log.info("Fetching match IDs for PUUID: {}", puuid);
        try {
            String url = ASIA_API_URL + "/lol/match/v5/matches/by-puuid/" + puuid + "/ids?queue=420&count=5";
            String[] matchIds = getRequest(url, String[].class);
            return matchIds != null ? Arrays.asList(matchIds) : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching match IDs: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public MatchDTO getMatchById(String matchId) {
        try {
            String url = ASIA_API_URL + "/lol/match/v5/matches/" + matchId;
            return getRequest(url, MatchDTO.class);
        } catch (Exception e) {
            log.error("Error fetching match data: {}", e.getMessage(), e);
            return null;
        }
    }

    // 매치 데이터에서 특정 소환사의 통계만 가져오는 메서드 추가
    public List<MatchDTO.ParticipantDTO> getParticipantStats(MatchDTO match, String summonerName) {
        if (match == null || match.getInfo() == null || match.getInfo().getParticipants() == null) {
            return new ArrayList<>();
        }

        return match.getInfo().getParticipants().stream()
                .filter(p -> summonerName.equals(p.getSummonerName()))
                .collect(Collectors.toList());
    }

    public SummonerDTO getSummonerByPuuid(String puuid) {
        log.info("Fetching summoner data for PUUID: {}", puuid);
        try {
            String url = KR_API_URL + "/lol/summoner/v4/summoners/by-puuid/" + puuid;
            SummonerDTO summoner = getRequest(url, SummonerDTO.class);

            if (summoner == null) {
                log.warn("No summoner found for PUUID: {}", puuid);
                return null;
            }

            log.info("Successfully retrieved summoner data for PUUID: {}", puuid);
            return summoner;
        } catch (Exception e) {
            log.error("Error fetching summoner data by PUUID: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 소환사의 이름과 태그를 사용하여 PUUID를 가져오는 메서드
     * 
     * @param summonerNameWithTag 태그가 포함된 소환사 이름 (예: "이름#태그")
     * @return 소환사의 PUUID
     */
    public String getRiotIdByNameAndTag(String summonerNameWithTag) {
        try {
            log.info("Getting PUUID for Riot ID: {}", summonerNameWithTag);

            // 이름과 태그 분리
            String[] parts = summonerNameWithTag.split("#");
            if (parts.length != 2) {
                log.error("Invalid Riot ID format: {}", summonerNameWithTag);
                return null;
            }

            String gameName = parts[0];
            String tagLine = parts[1];

            // Riot Account API 사용
            String url = "https://asia.api.riotgames.com/riot/account/v1/accounts/by-riot-id/"
                    + WebUtils.encodeURIComponent(gameName) + "/"
                    + WebUtils.encodeURIComponent(tagLine);

            RiotAccountDTO response = getRequest(url, RiotAccountDTO.class);

            if (response != null) {
                log.info("Found PUUID for Riot ID {}: {}", summonerNameWithTag, response.getPuuid());
                return response.getPuuid();
            }

            return null;
        } catch (Exception e) {
            log.error("Error getting PUUID for Riot ID: {}", summonerNameWithTag, e);
            return null;
        }
    }
}