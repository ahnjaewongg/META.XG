package com.lolservice.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import com.lolservice.dto.MatchDTO;
import com.lolservice.entity.ChallengerParticipantEntity;
import com.lolservice.entity.ChampionBanEntity;
import com.lolservice.repository.MatchRepository;
import com.lolservice.repository.ChampionBanRepository;
import com.lolservice.repository.ChallengerParticipantRepository;

@Service
@Slf4j
@EnableScheduling
public class MatchDataCollectorService {
    private final RiotApiService riotApiService;
    private final MatchRepository matchRepository;
    private final ChampionStatisticsService championStatisticsService;
    private final ChampionBanRepository championBanRepository;
    private final ChallengerParticipantRepository challengerParticipantRepository;

    public MatchDataCollectorService(RiotApiService riotApiService,
            MatchRepository matchRepository,
            ChampionStatisticsService championStatisticsService,
            ChampionBanRepository championBanRepository,
            ChallengerParticipantRepository challengerParticipantRepository) {
        this.riotApiService = riotApiService;
        this.matchRepository = matchRepository;
        this.championStatisticsService = championStatisticsService;
        this.championBanRepository = championBanRepository;
        this.challengerParticipantRepository = challengerParticipantRepository;
    }

    @Scheduled(initialDelay = 100000000, fixedRate = 1200000)
    public void collectMatchData() {
        try {
            List<String> summonerIds = riotApiService.getChallengerPlayerIds();
            Set<String> processedMatchIds = new HashSet<>();
            int totalNewMatches = 0;

            // 챌린저 상위 10명의 데이터 수집
            for (String summonerId : summonerIds.subList(0, 10)) {
                try {
                    String puuid = riotApiService.getPuuid(summonerId);
                    // 각 소환사당 최근 10게임 조회
                    List<String> matchIds = riotApiService.getMatchList(puuid, 5);
                    int newMatchCount = 0;

                    for (String matchId : matchIds) {
                        try {
                            if (processedMatchIds.contains(matchId)) {
                                continue;
                            }

                            if (matchRepository.existsByMatchId(matchId)) {
                                processedMatchIds.add(matchId);
                                continue;
                            }

                            Thread.sleep(1500);
                            processMatch(matchId);
                            processedMatchIds.add(matchId);
                            newMatchCount++;
                            totalNewMatches++;

                            // 각 소환사당 최대 3개의 새로운 매치 수집
                            if (newMatchCount >= 3) {
                                break;
                            }

                            // 전체 15개의 새로운 매치를 수집하면 종료
                            if (totalNewMatches >= 15) {
                                log.info("Collected maximum number of new matches (15)");
                                break;
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        } catch (Exception e) {
                            log.error("Error processing match {}: {}", matchId, e.getMessage());
                        }
                    }

                    Thread.sleep(2000); // 소환사 간 간격

                    if (totalNewMatches >= 15) {
                        break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    log.error("Error processing summoner {}: {}", summonerId, e.getMessage());
                }
            }

            log.info("Collected {} new matches in total", totalNewMatches);
            championStatisticsService.calculateChampionStatistics();
        } catch (Exception e) {
            log.error("Failed to collect match data: {}", e.getMessage(), e);
        }
    }

    private void processMatch(String matchId) {
        try {
            if (matchRepository.existsByMatchId(matchId)) {
                return;
            }
            MatchDTO matchDTO = riotApiService.getMatch(matchId);
            if (matchDTO == null || matchDTO.getInfo() == null) {
                return;
            }

            // convertMatchToEntities를 사용하여 밴 정보를 포함한 엔티티 리스트 생성
            List<ChallengerParticipantEntity> participants = convertMatchToEntities(matchDTO);

            // 각 참가자 엔티티에 공통 정보 설정 및 저장
            participants.forEach(participant -> {
                participant.setMatchId(matchDTO.getMetadata().getMatchId());
                participant.setGameCreation(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(matchDTO.getInfo().getGameCreation()),
                        ZoneId.systemDefault()));
                participant.setGameDuration(matchDTO.getInfo().getGameDuration());
                participant.setGameVersion(matchDTO.getInfo().getGameVersion());
                participant.setQueueTypeFromId(matchDTO.getInfo().getQueueId());
                challengerParticipantRepository.save(participant);
            });

            log.info("Successfully saved match {} to database with ban information", matchId);
        } catch (Exception e) {
            log.error("Error processing match {}: {}", matchId, e.getMessage(), e);
        }
    }

    private List<ChallengerParticipantEntity> convertMatchToEntities(MatchDTO match) {
        List<ChallengerParticipantEntity> entities = new ArrayList<>();

        // 밴 정보 저장
        if (match.getInfo().getTeams() != null) {
            match.getInfo().getTeams().forEach(team -> {
                if (team.getBans() != null) {
                    team.getBans().forEach(ban -> {
                        if (ban.getChampionId() > 0) {
                            ChampionBanEntity banEntity = new ChampionBanEntity(
                                    match.getMetadata().getMatchId(),
                                    String.valueOf(ban.getChampionId()),
                                    team.getTeamId(),
                                    ban.getPickTurn());
                            championBanRepository.save(banEntity);
                            log.info("Saved ban info for champion: {} in match: {}",
                                    ban.getChampionId(), match.getMetadata().getMatchId());
                        }
                    });
                }
            });
        }

        // 참가자 정보 변환
        for (MatchDTO.ParticipantDTO participant : match.getInfo().getParticipants()) {
            entities.add(convertParticipantToEntity(participant, match));
        }

        return entities;
    }

    private ChallengerParticipantEntity convertParticipantToEntity(MatchDTO.ParticipantDTO participant,
            MatchDTO match) {
        ChallengerParticipantEntity entity = new ChallengerParticipantEntity();

        // 소환사 정보 설정
        log.info("Converting participant - PUUID: {}", participant.getPuuid());
        entity.setPuuid(participant.getPuuid());

        // Riot ID 조회
        try {
            String riotId = riotApiService.getRiotId(participant.getPuuid());
            if (riotId != null) {
                entity.setSummonerName(riotId);
                log.info("Retrieved Riot ID for participant: {}", riotId);
            } else {
                entity.setSummonerName(participant.getSummonerName());
                log.warn("Failed to get Riot ID, using default summoner name: {}", participant.getSummonerName());
            }
        } catch (Exception e) {
            entity.setSummonerName(participant.getSummonerName());
            log.error("Error getting Riot ID for PUUID {}: {}", participant.getPuuid(), e.getMessage());
        }

        // 챔피언 정보 설정
        entity.setChampionId(String.valueOf(participant.getChampionId()));
        entity.setChampionName(participant.getChampionName());
        entity.setPosition(participant.getTeamPosition());
        entity.setWinner(participant.isWin());

        // 게임 스탯 설정
        entity.setKills(participant.getKills());
        entity.setDeaths(participant.getDeaths());
        entity.setAssists(participant.getAssists());
        entity.setTotalDamageDealtToChampions(participant.getTotalDamageDealtToChampions());
        entity.setVisionScore(participant.getVisionScore());
        entity.setGoldEarned(participant.getGoldEarned());
        entity.setTotalMinionsKilled(participant.getTotalMinionsKilled());
        entity.setNeutralMinionsKilled(participant.getNeutralMinionsKilled());

        // 멀티킬 정보 설정
        entity.setDoubleKills(participant.getDoubleKills());
        entity.setTripleKills(participant.getTripleKills());
        entity.setQuadraKills(participant.getQuadraKills());
        entity.setPentaKills(participant.getPentaKills());
        entity.setFirstBloodKill(participant.isFirstBloodKill());
        entity.setTurretKills(participant.getTurretKills());

        return entity;
    }
}