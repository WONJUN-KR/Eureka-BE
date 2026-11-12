package com.eureka.domain.place.service;

import com.eureka.domain.category.entity.SubCategory;
import com.eureka.domain.category.exception.CategoryException;
import com.eureka.domain.category.repository.SubCategoryRepository;
import com.eureka.domain.image.dto.ImageResponse;
import com.eureka.domain.image.entity.PostImage;
import com.eureka.domain.image.exception.ImageException;
import com.eureka.domain.image.repository.PostImageRepository;
import com.eureka.domain.image.service.ImageService;
import com.eureka.domain.place.dto.PlacePoint;
import com.eureka.domain.place.dto.PlaceRequest;
import com.eureka.domain.place.dto.PlaceResponse;
import com.eureka.domain.place.entity.PostPlace;
import com.eureka.domain.place.exception.PlaceException;
import com.eureka.domain.place.repository.PostPlaceRepository;
import com.eureka.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// 게시글 장소 - 게시글 작성 · 수정 트랜잭션 안에서 PostService 가 부름
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private static final int MAX_PLACE_COUNT = 20;

    private final PostPlaceRepository postPlaceRepository;
    private final PostImageRepository postImageRepository;
    private final SubCategoryRepository subCategoryRepository;

    @Transactional
    public void createPlaces(Post post, List<PlaceRequest> requests) {
        validateCount(requests);
        Map<Long, SubCategory> subCategories = findSubCategories(requests);
        List<PostPlace> places = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            places.add(newPlace(post, i + 1, requests.get(i), subCategories));
        }
        postPlaceRepository.saveAll(places);
    }

    // 요청 목록 순서대로 seq 를 다시 매기고, id 가 있으면 기존 장소 수정 · 없으면 새로 추가 · 목록에 없는 기존 장소는 삭제
    @Transactional
    public void replacePlaces(Post post, List<PlaceRequest> requests) {
        validateCount(requests);
        Map<Long, SubCategory> subCategories = findSubCategories(requests);
        Map<Long, PostPlace> existing = postPlaceRepository.findByPostIdOrderBySeqAsc(post.getId()).stream()
                .collect(Collectors.toMap(PostPlace::getId, Function.identity()));

        Set<Long> keptIds = new HashSet<>();
        List<PostPlace> newPlaces = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            PlaceRequest request = requests.get(i);
            int seq = i + 1;
            if (request.getId() == null) {
                newPlaces.add(newPlace(post, seq, request, subCategories));
                continue;
            }
            PostPlace place = existing.get(request.getId());
            if (place == null || !keptIds.add(place.getId())) {
                throw PlaceException.notFound();
            }
            place.update(seq, request.getName(), request.getAddress(), request.getLatitude(), request.getLongitude(),
                    request.getKakaoPlaceId(), subCategoryOf(request, subCategories), request.getMemo(), request.getVisitedAt());
        }

        List<PostPlace> removed = existing.values().stream()
                .filter(place -> !keptIds.contains(place.getId()))
                .toList();
        if (!removed.isEmpty()) {
            // 빠진 장소의 사진은 게시글 사진으로 남음 - 남는 사진까지 합쳐 장소 없는 사진 한도를 넘으면 수정 거부
            List<PostImage> detached = postImageRepository.findByPlaceIdIn(removed.stream().map(PostPlace::getId).toList());
            if (postImageRepository.countByPostIdAndPlaceIsNull(post.getId()) + detached.size() > ImageService.MAX_POST_IMAGE_COUNT) {
                throw ImageException.maxCountExceeded();
            }
            detached.forEach(PostImage::detachPlace);
            postPlaceRepository.deleteAll(removed);
        }
        postPlaceRepository.saveAll(newPlaces);
    }

    // 상세용 - 장소마다 그 장소에 붙은 사진을 같이 담음
    public List<PlaceResponse> getPlaces(Long postId) {
        List<PostPlace> places = postPlaceRepository.findByPostIdOrderBySeqAsc(postId);
        Map<Long, List<ImageResponse>> imagesByPlace = new HashMap<>();
        for (PostImage image : postImageRepository.findByPostIdOrderBySeqAsc(postId)) {
            if (image.getPlace() != null) {
                imagesByPlace.computeIfAbsent(image.getPlace().getId(), k -> new ArrayList<>()).add(ImageResponse.from(image));
            }
        }
        return places.stream()
                .map(place -> PlaceResponse.from(place, imagesByPlace.getOrDefault(place.getId(), List.of())))
                .toList();
    }

    // 목록용 - 게시글별 장소 이름 · 위치를 한 번에 조회
    public Map<Long, List<PlacePoint>> getPointsByPostIds(Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<PlacePoint>> points = new HashMap<>();
        for (PostPlace place : postPlaceRepository.findByPostIdInOrderBySeqAsc(postIds)) {
            points.computeIfAbsent(place.getPost().getId(), k -> new ArrayList<>()).add(PlacePoint.from(place));
        }
        return points;
    }

    private void validateCount(List<PlaceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw PlaceException.required();
        }
        if (requests.size() > MAX_PLACE_COUNT) {
            throw PlaceException.maxCountExceeded();
        }
    }

    private Map<Long, SubCategory> findSubCategories(List<PlaceRequest> requests) {
        Set<Long> ids = requests.stream()
                .map(PlaceRequest::getSubCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SubCategory> found = subCategoryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(SubCategory::getId, Function.identity()));
        if (found.size() != ids.size()) {
            throw CategoryException.notFound();
        }
        return found;
    }

    private SubCategory subCategoryOf(PlaceRequest request, Map<Long, SubCategory> subCategories) {
        return request.getSubCategoryId() != null ? subCategories.get(request.getSubCategoryId()) : null;
    }

    private PostPlace newPlace(Post post, int seq, PlaceRequest request, Map<Long, SubCategory> subCategories) {
        return PostPlace.builder()
                .post(post)
                .seq(seq)
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .kakaoPlaceId(request.getKakaoPlaceId())
                .subCategory(subCategoryOf(request, subCategories))
                .memo(request.getMemo())
                .visitedAt(request.getVisitedAt())
                .build();
    }
}
