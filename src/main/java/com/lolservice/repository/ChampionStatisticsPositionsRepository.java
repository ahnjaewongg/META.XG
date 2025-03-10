package com.lolservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lolservice.entity.ChampionStatisticsPositionsEntity;

@Repository
public interface ChampionStatisticsPositionsRepository extends JpaRepository<ChampionStatisticsPositionsEntity, Long> {
}