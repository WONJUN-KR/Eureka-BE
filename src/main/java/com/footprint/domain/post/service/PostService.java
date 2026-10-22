package com.footprint.domain.post.service;

import com.footprint.domain.category.entity.SubCategory;
import com.footprint.domain.category.exception.CategoryException;
import com.footprint.domain.category.repository.SubCategoryRepository;
import com.footprint.domain.image.entity.PostImage;
import com.footprint.domain.image.repository.PostImageRepository;
import com.footprint.domain.post.dto.*;
import com.footprint.domain.post.entity.Post;
import com.footprint.domain.post.exception.PostException;
import com.footprint.domain.post.repository.PostRepository;
import com.footprint.domain.region.entity.City;
import com.footprint.domain.region.exception.RegionException;
import com.footprint.domain.region.repository.CityRepository;
import com.footprint.domain.user.entity.User;
import com.footprint.domain.user.exception.UserException;
import com.footprint.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
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

        return PostDetailResponse.from(post);
    }

    public Page<PostResponse> getPosts(Long regionId, Long cityId, Long categoryId, Long subCategoryId, Pageable pageable) {
        return toResponsePage(postRepository.findAllWithFilters(regionId, cityId, categoryId, subCategoryId, pageable));
    }

    public Page<PostResponse> getMyPosts(Long userId, Pageable pageable) {
        return toResponsePage(postRepository.findByUserId(userId, pageable));
    }

    // 페이지의 게시글 이미지를 한 번에 조회해 게시글별 첫 이미지를 썸네일로 사용
    private Page<PostResponse> toResponsePage(Page<Post> posts) {
        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Map<Long, String> thumbnails = new HashMap<>();
        if (!postIds.isEmpty()) {
            for (PostImage image : postImageRepository.findByPostIdInOrderBySeqAsc(postIds)) {
                thumbnails.putIfAbsent(image.getPost().getId(), image.getUrl());
            }
        }
        return posts.map(post -> PostResponse.from(post, thumbnails.get(post.getId())));
    }

    @Transactional
    public PostDetailResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostException::notFound);
        post.incrementViewCount();
        return PostDetailResponse.from(post);
    }

    @Transactional
    public PostDetailResponse updatePost(Long userId, Long postId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
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

        return PostDetailResponse.from(post);
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
