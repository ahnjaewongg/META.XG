package com.lolservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lolservice.entity.PostEntity;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 포지션별 게시글 조회
    List<PostEntity> findByPositionOrderByCreatedAtDesc(String position);

    // 모든 게시글 최신순 조회
    List<PostEntity> findAllByOrderByCreatedAtDesc();

    // 추천 게시글 조회
    List<PostEntity> findByIsFeaturedTrueOrderByCreatedAtDesc();

    // 좋아요 순으로 인기 게시글 조회
    List<PostEntity> findTop10ByOrderByLikesDesc();
}