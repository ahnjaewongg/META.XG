package com.lolservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lolservice.dto.CommentDTO;
import com.lolservice.dto.PostDTO;
import com.lolservice.entity.CommentEntity;
import com.lolservice.entity.PostEntity;
import com.lolservice.repository.CommentRepository;
import com.lolservice.repository.PostRepository;

@Service
public class CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    // 모든 게시글 조회
    @Transactional(readOnly = true)
    public List<PostDTO> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());
    }

    // 포지션별 게시글 조회
    @Transactional(readOnly = true)
    public List<PostDTO> getPostsByPosition(String position) {
        return postRepository.findByPositionOrderByCreatedAtDesc(position).stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());
    }

    // 추천 게시글 조회
    @Transactional(readOnly = true)
    public List<PostDTO> getFeaturedPosts() {
        return postRepository.findByIsFeaturedTrueOrderByCreatedAtDesc().stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());
    }

    // 인기 게시글 조회
    @Transactional(readOnly = true)
    public List<PostDTO> getPopularPosts() {
        return postRepository.findTop10ByOrderByLikesDesc().stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    @Transactional
    public PostDTO getPostById(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 조회수 증가
        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        return convertToPostDTO(post);
    }

    // 게시글 생성
    @Transactional
    public PostDTO createPost(PostDTO postDTO) {
        PostEntity post = PostEntity.builder()
                .title(postDTO.getTitle())
                .content(postDTO.getContent())
                .author(postDTO.getAuthor())
                .position(postDTO.getPosition())
                .comments(new ArrayList<>())
                .build();

        PostEntity savedPost = postRepository.save(post);
        return convertToPostDTO(savedPost);
    }

    // 게시글 좋아요 증가
    @Transactional
    public PostDTO likePost(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setLikes(post.getLikes() + 1);
        PostEntity updatedPost = postRepository.save(post);

        return convertToPostDTO(updatedPost);
    }

    // 게시글 추천 상태 변경
    @Transactional
    public PostDTO toggleFeatured(Long id) {
        PostEntity post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        post.setFeatured(!post.isFeatured());
        PostEntity updatedPost = postRepository.save(post);

        return convertToPostDTO(updatedPost);
    }

    // 댓글 생성
    @Transactional
    public CommentDTO createComment(Long postId, CommentDTO commentDTO) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        CommentEntity comment = CommentEntity.builder()
                .post(post)
                .author(commentDTO.getAuthor())
                .content(commentDTO.getContent())
                .build();

        CommentEntity savedComment = commentRepository.save(comment);
        return convertToCommentDTO(savedComment);
    }

    // 댓글 좋아요 증가
    @Transactional
    public CommentDTO likeComment(Long id) {
        CommentEntity comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        comment.setLikes(comment.getLikes() + 1);
        CommentEntity updatedComment = commentRepository.save(comment);

        return convertToCommentDTO(updatedComment);
    }

    // Entity -> DTO 변환 메서드
    private PostDTO convertToPostDTO(PostEntity post) {
        List<CommentDTO> commentDTOs = post.getComments() != null
                ? post.getComments().stream()
                        .map(this::convertToCommentDTO)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .position(post.getPosition())
                .likes(post.getLikes())
                .views(post.getViews())
                .isFeatured(post.isFeatured())
                .comments(commentDTOs)
                .commentCount(commentDTOs.size())
                .build();
    }

    private CommentDTO convertToCommentDTO(CommentEntity comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .author(comment.getAuthor())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .likes(comment.getLikes())
                .build();
    }
}