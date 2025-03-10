package com.lolservice.repository;

import com.lolservice.entity.MatchParticipantEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 매치 참가자 데이터 관련 데이터베이스 접근을 담당하는 레포지토리
 */
@Repository
public interface MatchParticipantRepository extends JpaRepository<MatchParticipantEntity, Long> {
    List<MatchParticipantEntity> findBySummonerName(String summonerName);

    List<MatchParticipantEntity> findBySummonerNameAndCreatedAtAfter(String summonerName, long date);

    void deleteBySummonerNameAndCreatedAtBefore(String summonerName, long date);

    boolean existsByMatchIdAndPuuid(String matchId, String puuid);

    List<MatchParticipantEntity> findBySummonerNameOrderByCreatedAtDesc(String summonerName, Pageable pageable);

    /**
     * 매치 ID 목록과 소환사 ID로 매치 참가자 정보 조회
     * 
     * @param matchIds 매치 ID 목록
     * @param puuid    소환사 ID
     * @return 매치 참가자 목록
     */
    @Query("SELECT mp FROM MatchParticipantEntity mp " +
            "WHERE mp.matchId IN :matchIds " +
            "AND mp.puuid = :puuid " +
            "ORDER BY mp.createdAt DESC")
    List<MatchParticipantEntity> findByMatchIdsAndSummonerId(
            @Param("matchIds") List<String> matchIds,
            @Param("puuid") String puuid);

    /**
     * 특정 소환사가 최근에 가장 많이 플레이한 챔피언 조회
     * 
     * @param puuid    소환사 ID
     * @param pageable 페이지 정보
     * @return 챔피언 ID와 플레이 횟수
     */
    @Query(value = "SELECT mp.champion_id, COUNT(*) as play_count " +
            "FROM match_participants mp " +
            "WHERE mp.puuid = :puuid " +
            "GROUP BY mp.champion_id " +
            "ORDER BY play_count DESC", nativeQuery = true)
    List<Object[]> findMostPlayedChampions(@Param("puuid") String puuid, Pageable pageable);

    @Query("SELECT mp FROM MatchParticipantEntity mp " +
            "WHERE mp.puuid = :puuid " +
            "ORDER BY mp.createdAt DESC")
    List<MatchParticipantEntity> findRecentMatchesBySummonerId(@Param("puuid") String puuid,
            Pageable pageable);

    boolean existsByMatchId(String matchId);

    List<MatchParticipantEntity> findBySummonerNameIgnoreCase(String summonerName);

    /**
     * PUUID를 기준으로 참가자 정보 검색
     */
    List<MatchParticipantEntity> findByPuuid(String puuid);
}