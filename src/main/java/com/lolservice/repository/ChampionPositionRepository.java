package com.lolservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lolservice.entity.ChampionPositionEntity;

import java.util.List;

@Repository
public interface ChampionPositionRepository extends JpaRepository<ChampionPositionEntity, Long> {

    /**
     * 챔피언 이름으로 주 포지션을 찾는 메서드
     * 
     * @param championName 챔피언 이름
     * @return 주 포지션 (TOP, JUNGLE, MIDDLE, BOTTOM, UTILITY 중 하나)
     */
    @Query("SELECT cp.position FROM ChampionPositionEntity cp WHERE cp.championName = :championName AND cp.pickRate = (SELECT MAX(cp2.pickRate) FROM ChampionPositionEntity cp2 WHERE cp2.championName = :championName)")
    String findMainPositionByChampionName(@Param("championName") String championName);

    /**
     * 챔피언 ID로 주 포지션을 찾는 메서드
     * 
     * @param championId 챔피언 ID
     * @return 주 포지션 (TOP, JUNGLE, MIDDLE, BOTTOM, UTILITY 중 하나)
     */
    @Query("SELECT cp.position FROM ChampionPositionEntity cp WHERE cp.championId = :championId AND cp.pickRate = (SELECT MAX(cp2.pickRate) FROM ChampionPositionEntity cp2 WHERE cp2.championId = :championId)")
    String findMainPositionByChampionId(@Param("championId") int championId);

    /**
     * 챔피언 이름으로 해당 챔피언의 모든 포지션 정보를 가져오는 메서드
     * 
     * @param championName 챔피언 이름
     * @return 해당 챔피언의 모든 포지션 정보 리스트
     */
    List<ChampionPositionEntity> findByChampionName(String championName);
}