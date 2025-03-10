package com.lolservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lolservice.entity.ChampionBanEntity;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChampionBanRepository extends JpaRepository<ChampionBanEntity, Long> {
    @Query("SELECT b.championId FROM ChampionBanEntity b WHERE b.matchId IN " +
            "(SELECT DISTINCT p.matchId FROM ChallengerParticipantEntity p WHERE p.gameCreation >= :cutoff)")
    List<String> findBannedChampionsAfter(@Param("cutoff") LocalDateTime cutoff);
}