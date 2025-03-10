package com.lolservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lolservice.entity.CommentEntity;
import com.lolservice.entity.PostEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 특정 게시글의 댓글 조회
    List<CommentEntity> findByPostOrderByCreatedAtAsc(PostEntity post);

    // 특정 게시글의 댓글 수 조회
    long countByPost(PostEntity post);
}