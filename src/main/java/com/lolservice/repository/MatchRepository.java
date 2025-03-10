package com.lolservice.repository;

import com.lolservice.entity.ChallengerParticipantEntity;
import com.lolservice.entity.MatchParticipantEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 매치 데이터 관련 데이터베이스 접근을 담당하는 레포지토리
 */
@Repository
public interface MatchRepository extends JpaRepository<MatchParticipantEntity, Long> {
        @Query("SELECT p FROM ChallengerParticipantEntity p WHERE p.gameCreation >= :cutoff")
        List<ChallengerParticipantEntity> findMatchesAfter(@Param("cutoff") LocalDateTime cutoff);

        boolean existsByMatchId(String matchId);

        @Query("SELECT p FROM ChallengerParticipantEntity p " +
                        "WHERE p.gameCreation >= :cutoff " +
                        "ORDER BY p.gameCreation DESC")
        Set<ChallengerParticipantEntity> findMatchesAfterWithDetails(@Param("cutoff") LocalDateTime cutoff);

        @Query("SELECT p FROM ChallengerParticipantEntity p " +
                        "WHERE p.position = :position " +
                        "AND p.gameCreation >= :cutoffDate " +
                        "ORDER BY p.gameCreation DESC")
        List<ChallengerParticipantEntity> findChallengerMatches(
                        @Param("position") String position,
                        @Param("cutoffDate") LocalDateTime cutoffDate);

        @Query("SELECT p FROM ChallengerParticipantEntity p " +
                        "WHERE p.position = :position " +
                        "AND p.championName <> :excludeChampion " +
                        "AND p.gameCreation >= :cutoffDate " +
                        "ORDER BY p.gameCreation DESC")
        List<ChallengerParticipantEntity> findChallengerMatchesByPositionExcludingChampion(
                        @Param("position") String position,
                        @Param("excludeChampion") String excludeChampion,
                        @Param("cutoffDate") LocalDateTime cutoffDate);

        @Query("SELECT p FROM ChallengerParticipantEntity p " +
                        "WHERE p.summonerName = :summonerName " +
                        "AND p.gameCreation > :cutoffDate " +
                        "ORDER BY p.gameCreation DESC")
        List<ChallengerParticipantEntity> findRecentMatchesBySummonerName(
                        @Param("summonerName") String summonerName,
                        @Param("cutoffDate") LocalDateTime cutoffDate,
                        Pageable pageable);

        @Query("DELETE FROM ChallengerParticipantEntity p WHERE p.gameCreation < :cutoffDate")
        @Modifying
        int deleteByGameCreationBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

        @Query("SELECT p FROM ChallengerParticipantEntity p " +
                        "WHERE p.summonerName = :summonerName " +
                        "ORDER BY p.gameCreation DESC")
        List<ChallengerParticipantEntity> findByParticipantsSummonerNameOrderByGameCreationDesc(
                        @Param("summonerName") String summonerName,
                        Pageable pageable);

        @Query("SELECT p FROM ChallengerParticipantEntity p " +
                        "ORDER BY p.gameCreation DESC")
        List<ChallengerParticipantEntity> findMatchesOrderByGameCreationDesc(Pageable pageable);

        @Query("SELECT p FROM ChallengerParticipantEntity p WHERE p.matchId = :matchId")
        List<ChallengerParticipantEntity> findByMatchId(@Param("matchId") String matchId);

        /**
         * 소환사 ID로 최근 매치 데이터 조회
         * 
         * @param summonerId 소환사 ID
         * @param pageable   페이징 정보
         * @return 최근 매치 목록
         */
        @Query(value = "SELECT m.* FROM matches m " +
                        "JOIN match_participants mp ON m.match_id = mp.match_id " +
                        "WHERE mp.summoner_id = :summonerId " +
                        "ORDER BY m.game_creation DESC", nativeQuery = true)
        List<MatchParticipantEntity> findRecentMatchesBySummonerId(@Param("summonerId") String summonerId,
                        Pageable pageable);

        /**
         * 특정 포지션을 플레이하는 챌린저 플레이어의 매치 데이터 조회 (특정 챔피언 제외)
         * 
         * @param position        포지션
         * @param excludeChampion 제외할 챔피언 ID
         * @return 챌린저 플레이어의 해당 포지션 매치 참여자 목록
         */
        @Query(value = "SELECT mp.* FROM match_participants mp " +
                        "JOIN matches m ON mp.match_id = m.match_id " +
                        "WHERE mp.team_position = :position " +
                        "AND mp.champion_id != :excludeChampion " +
                        "AND mp.tier = 'CHALLENGER' " +
                        "ORDER BY m.game_creation DESC", nativeQuery = true)
        List<MatchParticipantEntity> findChallengerMatchesByPositionExcludingChampion(
                        @Param("position") String position,
                        @Param("excludeChampion") String excludeChampion);
}