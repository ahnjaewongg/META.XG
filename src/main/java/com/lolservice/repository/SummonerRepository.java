package com.lolservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lolservice.entity.SummonerEntity;

public interface SummonerRepository extends JpaRepository<SummonerEntity, String> {
    SummonerEntity findByPuuid(String puuid);

    SummonerEntity findByName(String name);

    /**
     * 대소문자 구분 없이 소환사 이름으로 검색
     */
    SummonerEntity findByNameIgnoreCase(String name);
}