package com.lolservice.repository;

import com.lolservice.entity.ChallengerParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengerParticipantRepository extends JpaRepository<ChallengerParticipantEntity, Long> {
    // 여기에 필요한 사용자 정의 쿼리 메서드를 추가할 수 있습니다.
}