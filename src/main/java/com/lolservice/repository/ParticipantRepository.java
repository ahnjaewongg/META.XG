package com.lolservice.repository;

import com.lolservice.entity.ChallengerParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<ChallengerParticipantEntity, Long> {
    @Query("SELECT p FROM ChallengerParticipantEntity p " +
            "WHERE p.position = :position " +
            "AND p.gameCreation >= :cutoffDate " +
            "ORDER BY p.gameCreation DESC")
    List<ChallengerParticipantEntity> findChallengerMatches(
            @Param("position") String position,
            @Param("cutoffDate") LocalDateTime cutoffDate);
}