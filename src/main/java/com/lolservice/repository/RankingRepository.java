package com.lolservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lolservice.entity.RankingEntity;


@Repository
public interface RankingRepository extends JpaRepository<RankingEntity, String> {
    Page<RankingEntity> findAllByOrderByLeaguePointsDesc(Pageable pageable);

    @Query("DELETE FROM RankingEntity r WHERE r.tier = :tier")
    @Modifying
    void deleteByTier(String tier);
}