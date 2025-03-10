package com.lolservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lolservice.dto.CommentDTO;
import com.lolservice.dto.PostDTO;
import com.lolservice.service.CommunityService;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    // 모든 게시글 조회
    @GetMapping("/posts")
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        return ResponseEntity.ok(communityService.getAllPosts());
    }

    // 포지션별 게시글 조회
    @GetMapping("/posts/position/{position}")
    public ResponseEntity<List<PostDTO>> getPostsByPosition(@PathVariable String position) {
        return ResponseEntity.ok(communityService.getPostsByPosition(position));
    }

    // 추천 게시글 조회
    @GetMapping("/posts/featured")
    public ResponseEntity<List<PostDTO>> getFeaturedPosts() {
        return ResponseEntity.ok(communityService.getFeaturedPosts());
    }

    // 인기 게시글 조회
    @GetMapping("/posts/popular")
    public ResponseEntity<List<PostDTO>> getPopularPosts() {
        return ResponseEntity.ok(communityService.getPopularPosts());
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.getPostById(id));
    }

    // 게시글 생성
    @PostMapping("/posts")
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        return ResponseEntity.ok(communityService.createPost(postDTO));
    }

    // 게시글 좋아요 증가
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<PostDTO> likePost(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.likePost(id));
    }

    // 게시글 추천 상태 변경
    @PostMapping("/posts/{id}/toggle-featured")
    public ResponseEntity<PostDTO> toggleFeatured(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.toggleFeatured(id));
    }

    // 댓글 생성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long postId,
            @RequestBody CommentDTO commentDTO) {
        return ResponseEntity.ok(communityService.createComment(postId, commentDTO));
    }

    // 댓글 좋아요 증가
    @PostMapping("/comments/{id}/like")
    public ResponseEntity<CommentDTO> likeComment(@PathVariable Long id) {
        return ResponseEntity.ok(communityService.likeComment(id));
    }
}