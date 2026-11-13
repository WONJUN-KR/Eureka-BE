package com.eureka.domain.post.service;

import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.category.exception.CategoryException;
import com.eureka.domain.category.repository.SubCategoryRepository;
import com.eureka.domain.image.entity.PostImage;
import com.eureka.domain.image.repository.PostImageRepository;
import com.eureka.domain.like.repository.PostLikeRepository;
import com.eureka.domain.place.dto.PlacePoint;
import com.eureka.domain.place.service.PlaceService;
import com.eureka.domain.post.dto.*;
import com.eureka.domain.post.entity.Post;
import com.eureka.domain.post.exception.PostException;
import com.eureka.domain.post.repository.PostRepository;
import com.eureka.domain.region.entity.City;
import com.eureka.domain.region.exception.RegionException;
import com.eureka.domain.region.repository.CityRepository;
import com.eureka.domain.user.entity.User;
import com.eureka.domain.user.exception.UserException;
import com.eureka.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final PlaceService placeService;

    @Transactional
    public PostDetailResponse createPost(Long userId, CreatePostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException::notFound);
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(RegionException::notFound);
        SubCategory subCategory = subCategoryRepository.findById(request.getSubCategoryId())
                .orElseThrow(CategoryException::notFound);

        Post post = request.toEntity(user, city, subCategory);
        post.publish();
        postRepository.save(post);
        placeService.createPlaces(post, request.getPlaces());

        return PostDetailResponse.from(post, placeService.getPlaces(post.getId()));
    }

    public Page<PostResponse> getPosts(Long regionId, Long cityId, Long categoryId, Long subCategoryId, String keyword, Pageable pageable) {
        return toResponsePage(postRepository.findAllWithFilters(regionId, cityId, categoryId, subCategoryId, toLikePattern(keyword), pageable));
    }

    // 검색어 -> 대소문자 무시 부분 일치 패턴 (%, _ 는 글자 그대로 찾도록 이스케이프), 비어 있으면 검색 안 함
    private String toLikePattern(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        String escaped = keyword.strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    public Page<PostResponse> getMyPosts(Long userId, Pageable pageable) {
        return toResponsePage(postRepository.findByUserId(userId, pageable));
    }

    // 내가 좋아요한 글 - 공개 글만, 좋아요 누른 순으로 고정 (sort 파라미터는 무시)
    public Page<PostResponse> getMyLikedPosts(Long userId, Pageable pageable) {
        Pageable unsorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return toResponsePage(postLikeRepository.findLikedPosts(userId, unsorted));
    }

    // 다른 사람이 보는 사용자 글 목록 - 공개된 글만
    public Page<PostResponse> getUserPosts(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw UserException.notFound();
        }
        return toResponsePage(postRepository.findByUserIdAndStatus(userId, "PUBLISHED", pageable));
    }

    // 페이지의 게시글 이미지 · 장소를 한 번에 조회해 게시글별 첫 이미지를 썸네일로 사용
    private Page<PostResponse> toResponsePage(Page<Post> posts) {
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Map<Long, String> thumbnails = new HashMap<>();
        if (!postIds.isEmpty()) {
            for (PostImage image : postImageRepository.findByPostIdInOrderBySeqAsc(postIds)) {
                thumbnails.putIfAbsent(image.getPost().getId(), image.getUrl());
            }
        }
        Map<Long, List<PlacePoint>> places = placeService.getPointsByPostIds(postIds);
        return posts.map(post -> PostResponse.from(post, thumbnails.get(post.getId()), places.getOrDefault(post.getId(), List.of())));
    }

    @Transactional
    public PostDetailResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostException::notFound);
        post.incrementViewCount();
        return PostDetailResponse.from(post, placeService.getPlaces(postId));
    }

    @Transactional
    public PostDetailResponse updatePost(Long userId, Long postId, UpdatePostRequest request) {
        // 사진 업로드와 겹치지 않게 게시글 행을 잠그고 진행
        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(PostException::notFound);

        if (!post.isOwner(userId)) {
            throw PostException.unauthorized();
        }

        City city = request.getCityId() != null
                ? cityRepository.findById(request.getCityId()).orElseThrow(RegionException::notFound)
                : post.getCity();
        SubCategory subCategory = request.getSubCategoryId() != null
                ? subCategoryRepository.findById(request.getSubCategoryId()).orElseThrow(CategoryException::notFound)
                : post.getSubCategory();

        post.update(
                city,
                subCategory,
                request.getTitle() != null ? request.getTitle() : post.getTitle(),
                request.getContent() != null ? request.getContent() : post.getContent(),
                request.getSummary() != null ? request.getSummary() : post.getSummary()
        );
        if (request.getPlaces() != null) {
            placeService.replacePlaces(post, request.getPlaces());
        }

        return PostDetailResponse.from(post, placeService.getPlaces(postId));
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostException::notFound);

        if (!post.isOwner(userId)) {
            throw PostException.unauthorized();
        }

        post.delete();
    }
}
