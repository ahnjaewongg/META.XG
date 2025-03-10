package com.lolservice.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import com.lolservice.repository.SummonerRepository;
import com.lolservice.entity.SummonerEntity;
import com.lolservice.dto.SummonerDTO;

import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

import com.lolservice.dto.MatchDTO;
import com.lolservice.entity.MatchParticipantEntity;
import com.lolservice.repository.MatchParticipantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class SummonerService {
    private final MatchParticipantRepository matchParticipantRepository;
    private final RiotApiService riotApiService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SummonerRepository summonerRepository;
    private static final Logger log = LoggerFactory.getLogger(SummonerService.class);

    public SummonerService(
            MatchParticipantRepository matchParticipantRepository,
            RiotApiService riotApiService,
            RedisTemplate<String, Object> redisTemplate,
            SummonerRepository summonerRepository) {
        this.matchParticipantRepository = matchParticipantRepository;
        this.riotApiService = riotApiService;
        this.redisTemplate = redisTemplate;
        this.summonerRepository = summonerRepository;
    }

    public void saveSummoner(SummonerDTO summonerDTO) {
        log.info("Saving summoner to database: {}", summonerDTO.getName());

        // DTO를 Entity로 변환
        SummonerEntity entity = new SummonerEntity();
        entity.setId(summonerDTO.getId());
        entity.setAccountId(summonerDTO.getAccountId());
        entity.setPuuid(summonerDTO.getPuuid());
        entity.setName(summonerDTO.getName());
        entity.setProfileIconId(summonerDTO.getProfileIconId());
        entity.setRevisionDate(summonerDTO.getRevisionDate());
        entity.setSummonerLevel(summonerDTO.getSummonerLevel());
        entity.setLastUpdated(System.currentTimeMillis());

        // 데이터베이스에 저장
        summonerRepository.save(entity);
        log.info("Summoner saved successfully: {}", entity.getName());
    }

    public void saveMatchParticipants(String summonerName, List<MatchDTO> matches) {
        // 일주일 이전 데이터 삭제
        long oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        matchParticipantRepository.deleteBySummonerNameAndCreatedAtBefore(summonerName, oneWeekAgo);

        List<MatchParticipantEntity> participants = matches.stream()
                .flatMap(match -> match.getInfo().getParticipants().stream()
                        .map(participant -> {
                            // 기존 데이터 확인
                            String matchId = match.getMetadata().getMatchId();
                            String participantPuuid = participant.getPuuid();

                            // 이미 저장된 참가자 정보가 있는지 확인
                            if (matchParticipantRepository.existsByMatchIdAndPuuid(matchId, participantPuuid)) {
                                log.info("Participant already exists for match {} and PUUID {}", matchId,
                                        participantPuuid);
                                return null;
                            }

                            MatchParticipantEntity matchParticipant = new MatchParticipantEntity();
                            // PUUID로 Riot ID 조회
                            String riotId = riotApiService.getRiotId(participant.getPuuid());
                            matchParticipant.setSummonerName(riotId != null ? riotId : participant.getPuuid());
                            matchParticipant.setPuuid(participant.getPuuid());
                            matchParticipant.setMatchId(matchId);
                            matchParticipant.setChampionName(participant.getChampionName());
                            matchParticipant.setChampionId(participant.getChampionId());
                            matchParticipant.setTeamPosition(participant.getTeamPosition());
                            matchParticipant.setWin(participant.isWin());
                            matchParticipant.setKills(participant.getKills());
                            matchParticipant.setDeaths(participant.getDeaths());
                            matchParticipant.setAssists(participant.getAssists());
                            matchParticipant
                                    .setTotalDamageDealtToChampions(participant.getTotalDamageDealtToChampions());
                            matchParticipant.setGoldEarned(participant.getGoldEarned());
                            matchParticipant.setVisionScore(participant.getVisionScore());
                            return matchParticipant;
                        }))
                .filter(participant -> participant != null)
                .collect(Collectors.toList());

        if (!participants.isEmpty()) {
            matchParticipantRepository.saveAll(participants);
            log.info("Saved {} new participants for summoner {}", participants.size(), summonerName);

            // Redis 캐시 업데이트
            String cacheKey = "summoner_matches:" + summonerName;
            redisTemplate.opsForValue().set(cacheKey, matches);
            redisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
            log.info("Updated Redis cache for summoner {}", summonerName);
        }
    }

    public void saveMatchParticipants(MatchDTO match, String summonerName) {
        if (match == null || match.getInfo() == null || match.getInfo().getParticipants() == null) {
            log.warn("Match data is incomplete for summoner: {}", summonerName);
            return;
        }

        List<MatchParticipantEntity> participants = match.getInfo().getParticipants().stream()
                .map(participant -> {
                    MatchParticipantEntity entity = new MatchParticipantEntity();
                    entity.setMatchId(match.getMetadata().getMatchId());

                    // PUUID로 소환사 정보 조회
                    try {
                        // Riot API에서 소환사 정보 조회
                        SummonerDTO summonerInfo = riotApiService.getSummonerByPuuid(participant.getPuuid());
                        if (summonerInfo != null) {
                            // Riot ID 조회
                            String riotId = riotApiService.getRiotId(participant.getPuuid());

                            // 소환사 이름과 ID 설정
                            entity.setSummonerName(riotId != null ? riotId : summonerInfo.getName());
                            log.info("Found summoner name: {} with ID: {} for PUUID: {}",
                                    entity.getSummonerName(), participant.getPuuid());
                        } else {
                            // 기본 값 사용
                            entity.setSummonerName(participant.getSummonerName());
                            log.warn("Could not find summoner info for PUUID: {}, using default name: {}",
                                    participant.getPuuid(), participant.getSummonerName());
                        }
                    } catch (Exception e) {
                        // 예외 발생 시 기본 값 사용
                        entity.setSummonerName(participant.getSummonerName());
                        log.error("Error fetching summoner info for PUUID: {}: {}",
                                participant.getPuuid(), e.getMessage());
                    }

                    entity.setPuuid(participant.getPuuid());
                    entity.setChampionName(participant.getChampionName());
                    entity.setChampionId(participant.getChampionId());
                    entity.setTeamPosition(participant.getTeamPosition());
                    entity.setWin(participant.isWin());
                    entity.setKills(participant.getKills());
                    entity.setDeaths(participant.getDeaths());
                    entity.setAssists(participant.getAssists());
                    entity.setTotalDamageDealtToChampions(participant.getTotalDamageDealtToChampions());
                    entity.setVisionScore(participant.getVisionScore());
                    entity.setGoldEarned(participant.getGoldEarned());
                    entity.setTotalMinionsKilled(participant.getTotalMinionsKilled());
                    entity.setNeutralMinionsKilled(participant.getNeutralMinionsKilled());
                    entity.setCreatedAt(System.currentTimeMillis());
                    return entity;
                })
                .collect(Collectors.toList());

        if (!participants.isEmpty()) {
            matchParticipantRepository.saveAll(participants);
            log.info("Saved {} participants for match {}", participants.size(), match.getMetadata().getMatchId());
        }
    }

    public SummonerEntity findSummonerByName(String summonerName) {
        return summonerRepository.findByName(summonerName);
    }

    /**
     * PUUID로 소환사 엔티티 조회
     */
    public SummonerEntity findSummonerByPuuid(String puuid) {
        return summonerRepository.findByPuuid(puuid);
    }

    /**
     * 소환사 엔티티 저장
     */
    public void saveSummoner(SummonerEntity summonerEntity) {
        summonerEntity.setLastUpdated(System.currentTimeMillis());
        summonerRepository.save(summonerEntity);
        log.info("소환사 정보 저장 완료: {}", summonerEntity.getName());
    }

    /**
     * 모든 소환사 목록 조회
     */
    public List<SummonerEntity> findAllSummoners() {
        return summonerRepository.findAll();
    }
}