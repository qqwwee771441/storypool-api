package com.wudc.storypool.domain.post.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.fairytale.entity.Fairytale;
import com.wudc.storypool.domain.fairytale.repository.FairytaleRepository;
import com.wudc.storypool.domain.post.controller.response.PostDetailResponse;
import com.wudc.storypool.domain.post.controller.response.PostListResponse;
import com.wudc.storypool.domain.post.entity.Like;
import com.wudc.storypool.domain.post.entity.Post;
import com.wudc.storypool.domain.post.repository.LikeRepository;
import com.wudc.storypool.domain.post.repository.PostRepository;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final FairytaleRepository fairytaleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PostListResponse getPostsList(String currentUserId, String sortBy, String keyword, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Post> posts;

        // 키워드 검색이 있는 경우
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                posts = postRepository.findByKeywordOrderByIdDesc(keyword.trim(), pageable);
            } else {
                posts = postRepository.findByKeywordAfterCursorOrderByIdDesc(keyword.trim(), afterCursor, pageable);
            }
        } 
        // 인기순 정렬
        else if ("popular".equals(sortBy)) {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                posts = postRepository.findAllOrderByPopularityDesc(pageable);
            } else {
                posts = postRepository.findAllAfterCursorOrderByPopularityDesc(afterCursor, pageable);
            }
        }
        // 최신순 정렬 (기본)
        else {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                posts = postRepository.findAllOrderByIdDesc(pageable);
            } else {
                posts = postRepository.findAllAfterCursorOrderByIdDesc(afterCursor, pageable);
            }
        }

        // 다음 페이지 존재 여부 확인
        boolean hasNext = posts.size() > limit;
        if (hasNext) {
            posts = posts.subList(0, limit);
        }
        String nextCursor = hasNext && !posts.isEmpty() ? 
                           posts.get(posts.size() - 1).getId() : null;

        // 사용자 정보와 좋아요 정보를 일괄 조회
        List<String> userIds = posts.stream().map(Post::getUserId).distinct().collect(Collectors.toList());
        List<String> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<String> fairytaleIds = posts.stream().map(Post::getFairytaleId).distinct().collect(Collectors.toList());

        Map<String, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Set<String> likedPostIds;
        if (currentUserId != null) {
            likedPostIds = new HashSet<>(likeRepository.findLikedPostIdsByUserIdAndPostIds(currentUserId, postIds));
        } else {
            likedPostIds = new HashSet<>();
        }

        Map<String, Fairytale> fairytaleMap = fairytaleRepository.findAllById(fairytaleIds).stream()
                .collect(Collectors.toMap(Fairytale::getId, fairytale -> fairytale));

        // PostItem 생성
        List<PostListResponse.PostItem> postItems = posts.stream()
                .map(post -> {
                    User author = userMap.get(post.getUserId());
                    Fairytale fairytale = fairytaleMap.get(post.getFairytaleId());
                    String thumbnailUrl = fairytale != null && fairytale.getThumbnailPage() != null ? 
                                         fairytale.getThumbnailPage().getImageUrl() : null;
                    
                    return new PostListResponse.PostItem(
                        post.getId(),
                        post.getTitle(),
                        post.getContentPreview(),
                        post.getTags(),
                        thumbnailUrl,
                        post.getViewCount(),
                        post.getCommentCount(),
                        post.getLikeCount(),
                        currentUserId != null && likedPostIds.contains(post.getId()),
                        currentUserId != null && currentUserId.equals(post.getUserId()),
                        author != null ? new PostListResponse.AuthorInfo(
                            author.getId(),
                            author.getEmail(),
                            author.getNickname(),
                            author.getProfileImageUrl()
                        ) : null,
                        post.getCreatedAtByLocalDateTime(),
                        post.getUpdatedAtByLocalDateTime()
                    );
                })
                .collect(Collectors.toList());

        return new PostListResponse(postItems, hasNext, nextCursor);
    }

    @Transactional
    public PostDetailResponse getPostDetail(String currentUserId, String postId) {
        // 게시글 조회 및 조회수 증가
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found: {}", postId);
                    return new BaseException(ErrorCode.POST_NOT_FOUND);
                });

        // 조회수 증가 (본인 게시글이 아닌 경우에만)
        if (currentUserId == null || !currentUserId.equals(post.getUserId())) {
            post.incrementViewCount();
            postRepository.save(post);
        }

        // 작성자 정보 조회
        User author = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // 동화 정보 조회
        Fairytale fairytale = fairytaleRepository.findById(post.getFairytaleId())
                .orElseThrow(() -> new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND));

        // 좋아요 여부 확인
        boolean isLiked = currentUserId != null && 
                         likeRepository.findByUserIdAndPostId(currentUserId, postId).isPresent();

        // 페이지 정보 변환
        List<PostDetailResponse.PageInfo> pageList = fairytale.getPageList().stream()
                .map(page -> new PostDetailResponse.PageInfo(
                    page.getPageIndex(),
                    page.getMood(),
                    page.getStory(),
                    page.getImageUrl()
                ))
                .collect(Collectors.toList());

        PostDetailResponse.FairytaleInfo fairytaleInfo = new PostDetailResponse.FairytaleInfo(
            fairytale.getId(),
            fairytale.getName(),
            fairytale.getPageNumber(),
            pageList,
            fairytale.getStatus(),
            fairytale.getMessage(),
            fairytale.getCreatedAtByLocalDateTime(),
            fairytale.getUpdatedAtByLocalDateTime()
        );

        return new PostDetailResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getTags(),
            post.getViewCount(),
            post.getCommentCount(),
            post.getLikeCount(),
            isLiked,
            currentUserId != null && currentUserId.equals(post.getUserId()),
            new PostDetailResponse.AuthorInfo(
                author.getId(),
                author.getEmail(),
                author.getNickname(),
                author.getProfileImageUrl()
            ),
            fairytaleInfo,
            post.getCreatedAtByLocalDateTime(),
            post.getUpdatedAtByLocalDateTime()
        );
    }

    @Transactional
    public Post createPost(String userId, String title, String content, String fairytaleId, List<String> tags) {
        // 동화 존재 여부 및 소유권 확인
        Fairytale fairytale = fairytaleRepository.findByIdAndUserId(fairytaleId, userId)
                .orElseThrow(() -> {
                    log.warn("Fairytale not found or access denied for user: {} fairytaleId: {}", userId, fairytaleId);
                    return new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND);
                });

        Post post = Post.create(userId, title, content, fairytaleId, tags);
        Post savedPost = postRepository.save(post);
        
        log.info("Post created successfully: {} by user: {}", savedPost.getId(), userId);
        return savedPost;
    }

    @Transactional
    public Post updatePost(String userId, String postId, String title, String content, String fairytaleId, List<String> tags) {
        // 게시글 존재 여부 및 소유권 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found: {}", postId);
                    return new BaseException(ErrorCode.POST_NOT_FOUND);
                });

        if (!userId.equals(post.getUserId())) {
            log.warn("Post update access denied for user: {} postId: {}", userId, postId);
            throw new BaseException(ErrorCode.NO_AUTHORIZATION);
        }

        // 동화 존재 여부 및 소유권 확인
        Fairytale fairytale = fairytaleRepository.findByIdAndUserId(fairytaleId, userId)
                .orElseThrow(() -> {
                    log.warn("Fairytale not found or access denied for user: {} fairytaleId: {}", userId, fairytaleId);
                    return new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND);
                });

        post.updateContent(title, content, fairytaleId, tags);
        Post updatedPost = postRepository.save(post);
        
        log.info("Post updated successfully: {} by user: {}", postId, userId);
        return updatedPost;
    }

    @Transactional
    public void deletePost(String userId, String postId) {
        // 게시글 존재 여부 및 소유권 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found: {}", postId);
                    return new BaseException(ErrorCode.POST_NOT_FOUND);
                });

        if (!userId.equals(post.getUserId())) {
            log.warn("Post delete access denied for user: {} postId: {}", userId, postId);
            throw new BaseException(ErrorCode.NO_AUTHORIZATION);
        }

        // 관련 좋아요 먼저 삭제
        likeRepository.deleteByPostId(postId);
        
        // 게시글 삭제
        postRepository.delete(post);
        
        log.info("Post deleted successfully: {} by user: {}", postId, userId);
    }

    @Transactional
    public boolean toggleLike(String userId, String postId) {
        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found for like toggle: {}", postId);
                    return new BaseException(ErrorCode.POST_NOT_FOUND);
                });

        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(userId, postId);
        
        if (existingLike.isPresent()) {
            // 좋아요 취소
            likeRepository.delete(existingLike.get());
            post.decrementLikeCount();
            postRepository.save(post);
            log.info("Like removed: postId={}, userId={}", postId, userId);
            return false;
        } else {
            // 좋아요 추가
            Like like = Like.create(userId, postId);
            likeRepository.save(like);
            post.incrementLikeCount();
            postRepository.save(post);
            log.info("Like added: postId={}, userId={}", postId, userId);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public boolean hasNextPage(String currentUserId, String sortBy, String keyword, String afterCursor, int limit) {
        List<Post> posts = getPostsForPagination(currentUserId, sortBy, keyword, afterCursor, limit + 1);
        return posts.size() > limit;
    }

    @Transactional(readOnly = true)
    public PostListResponse getMyPostsList(String userId, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Post> posts;

        if (afterCursor == null || afterCursor.trim().isEmpty()) {
            posts = postRepository.findByUserIdOrderByIdDesc(userId, pageable);
        } else {
            posts = postRepository.findByUserIdAfterCursorOrderByIdDesc(userId, afterCursor, pageable);
        }

        return buildPostListResponse(posts, userId, limit);
    }

    @Transactional(readOnly = true)
    public PostListResponse getLikedPostsList(String userId, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Post> posts;

        if (afterCursor == null || afterCursor.trim().isEmpty()) {
            posts = postRepository.findLikedPostsByUserIdOrderByLikeIdDesc(userId, pageable);
        } else {
            posts = postRepository.findLikedPostsByUserIdAfterCursorOrderByLikeIdDesc(userId, afterCursor, pageable);
        }

        return buildPostListResponse(posts, userId, limit);
    }

    private PostListResponse buildPostListResponse(List<Post> posts, String currentUserId, int limit) {
        // 다음 페이지 존재 여부 확인
        boolean hasNext = posts.size() > limit;
        if (hasNext) {
            posts = posts.subList(0, limit);
        }
        String nextCursor = hasNext && !posts.isEmpty() ? 
                           posts.get(posts.size() - 1).getId() : null;

        // 사용자 정보와 좋아요 정보를 일괄 조회
        List<String> userIds = posts.stream().map(Post::getUserId).distinct().collect(Collectors.toList());
        List<String> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<String> fairytaleIds = posts.stream().map(Post::getFairytaleId).distinct().collect(Collectors.toList());

        Map<String, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Set<String> likedPostIds;
        if (currentUserId != null) {
            likedPostIds = new HashSet<>(likeRepository.findLikedPostIdsByUserIdAndPostIds(currentUserId, postIds));
        } else {
            likedPostIds = new HashSet<>();
        }

        Map<String, Fairytale> fairytaleMap = fairytaleRepository.findAllById(fairytaleIds).stream()
                .collect(Collectors.toMap(Fairytale::getId, fairytale -> fairytale));

        // PostItem 생성
        List<PostListResponse.PostItem> postItems = posts.stream()
                .map(post -> {
                    User author = userMap.get(post.getUserId());
                    Fairytale fairytale = fairytaleMap.get(post.getFairytaleId());
                    String thumbnailUrl = fairytale != null && fairytale.getThumbnailPage() != null ? 
                                         fairytale.getThumbnailPage().getImageUrl() : null;
                    
                    return new PostListResponse.PostItem(
                        post.getId(),
                        post.getTitle(),
                        post.getContentPreview(),
                        post.getTags(),
                        thumbnailUrl,
                        post.getViewCount(),
                        post.getCommentCount(),
                        post.getLikeCount(),
                        currentUserId != null && likedPostIds.contains(post.getId()),
                        currentUserId != null && currentUserId.equals(post.getUserId()),
                        author != null ? new PostListResponse.AuthorInfo(
                            author.getId(),
                            author.getEmail(),
                            author.getNickname(),
                            author.getProfileImageUrl()
                        ) : null,
                        post.getCreatedAtByLocalDateTime(),
                        post.getUpdatedAtByLocalDateTime()
                    );
                })
                .collect(Collectors.toList());

        return new PostListResponse(postItems, hasNext, nextCursor);
    }

    private List<Post> getPostsForPagination(String currentUserId, String sortBy, String keyword, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                return postRepository.findByKeywordOrderByIdDesc(keyword.trim(), pageable);
            } else {
                return postRepository.findByKeywordAfterCursorOrderByIdDesc(keyword.trim(), afterCursor, pageable);
            }
        } else if ("popular".equals(sortBy)) {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                return postRepository.findAllOrderByPopularityDesc(pageable);
            } else {
                return postRepository.findAllAfterCursorOrderByPopularityDesc(afterCursor, pageable);
            }
        } else {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                return postRepository.findAllOrderByIdDesc(pageable);
            } else {
                return postRepository.findAllAfterCursorOrderByIdDesc(afterCursor, pageable);
            }
        }
    }
}