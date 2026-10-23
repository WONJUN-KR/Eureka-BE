package com.eureka.domain.image.service;

import com.eureka.domain.image.dto.ImageResponse;
import com.eureka.domain.image.entity.PostImage;
import com.eureka.domain.image.exception.ImageException;
import com.eureka.domain.image.repository.PostImageRepository;
import com.eureka.domain.image.storage.ImageStorage;
import com.eureka.domain.post.entity.Post;
import com.eureka.domain.post.exception.PostException;
import com.eureka.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private static final int MAX_IMAGE_COUNT = 10;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final ImageStorage imageStorage;

    @Transactional
    public ImageResponse uploadImage(Long userId, Long postId, MultipartFile file) {
        Post post = postRepository.findById(postId)
                .orElseThrow(PostException::notFound);

        if (!post.isOwner(userId)) {
            throw PostException.unauthorized();
        }

        int currentCount = postImageRepository.countByPostId(postId);
        if (currentCount >= MAX_IMAGE_COUNT) {
            throw ImageException.maxCountExceeded();
        }

        validateFileType(file);

        String url = imageStorage.store(file, buildKey(file, postId));
        Integer maxSeq = postImageRepository.findMaxSeqByPostId(postId);
        int seq = (maxSeq == null ? 0 : maxSeq) + 1;

        PostImage image = PostImage.builder()
                .post(post)
                .url(url)
                .seq(seq)
                .build();
        postImageRepository.save(image);

        return ImageResponse.from(image);
    }

    @Transactional
    public void deleteImage(Long userId, Long imageId) {
        PostImage image = postImageRepository.findById(imageId)
                .orElseThrow(ImageException::notFound);

        if (!image.getPost().isOwner(userId)) {
            throw ImageException.unauthorized();
        }

        imageStorage.delete(image.getUrl());
        postImageRepository.delete(image);
    }

    public List<ImageResponse> getImages(Long postId) {
        return postImageRepository.findByPostIdOrderBySeqAsc(postId).stream()
                .map(ImageResponse::from)
                .toList();
    }

    private void validateFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            throw ImageException.invalidFileType();
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw ImageException.invalidFileType();
        }
    }

    private String buildKey(MultipartFile file, Long postId) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw ImageException.invalidFileType();
        }
        String extension = filename.substring(filename.lastIndexOf("."));
        return "posts/" + postId + "/" + UUID.randomUUID() + extension;
    }
}
