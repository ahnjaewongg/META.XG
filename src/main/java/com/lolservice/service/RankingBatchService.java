package com.lolservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lolservice.dto.LeagueEntryDTO;
import com.lolservice.entity.RankingEntity;
import com.lolservice.repository.RankingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankingBatchService {
    @Autowired
    private final RiotApiService riotApiService;
    @Autowired
    private final RankingRepository rankingRepository;

    @Scheduled(initialDelay = 0, fixedRate = 1800000)
    @Transactional
    public void updateRankings() {
        try {
            updateChallengerRankings();
        } catch (Exception e) {
            log.error("Error updating rankings: {}", e.getMessage());
        }
    }

    private void updateChallengerRankings() {
        try {
            List<LeagueEntryDTO> rankings = riotApiService.getChallengerLeague();

            // 기존 챌린저 데이터 삭제 - 문자열 "tier"가 아닌 "CHALLENGER"로 수정
            rankingRepository.deleteByTier("CHALLENGER");
            rankingRepository.deleteByTier("GRANDMASTER");

            // 새로운 데이터 저장
            List<RankingEntity> entities = rankings.stream()
                    .map(this::toEntity)
                    .collect(Collectors.toList());
            rankingRepository.saveAll(entities);

            entities.stream()
                    .limit(5)
                    .forEach(entity -> log.info("Saved ranking - Name: {}, LP: {}, Tier: {}",
                            entity.getSummonerName(),
                            entity.getLeaguePoints(),
                            entity.getTier()));

        } catch (Exception e) {
            log.error("Error updating Challenger rankings: {}", e.getMessage());
        }
    }

    private RankingEntity toEntity(LeagueEntryDTO dto) {
        RankingEntity entity = new RankingEntity();
        entity.setSummonerId(dto.getSummonerId());
        entity.setSummonerName(dto.getSummonerName());
        entity.setQueueType("RANKED_SOLO_5x5");
        entity.setTier("CHALLENGER");
        entity.setRank(dto.getRank());
        entity.setLeaguePoints(dto.getLeaguePoints());
        entity.setWins(dto.getWins());
        entity.setLosses(dto.getLosses());
        entity.setVeteran(dto.getVeteran());
        entity.setInactive(dto.getInactive());
        entity.setFreshBlood(dto.getFreshBlood());
        entity.setHotStreak(dto.getHotStreak());

        log.info("Converting DTO to Entity - Name: {}, LP: {}",
                dto.getSummonerName(), dto.getLeaguePoints());

        return entity;
    }
}