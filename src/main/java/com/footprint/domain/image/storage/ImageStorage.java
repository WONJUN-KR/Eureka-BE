package com.footprint.domain.image.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 게시글 이미지 파일 저장소 - 운영은 S3, local 프로필은 로컬 디스크
 */
public interface ImageStorage {

    /**
     * 파일을 key 경로에 저장하고 접근 가능한 URL 을 반환
     */
    String store(MultipartFile file, String key);

    /**
     * store 가 반환한 URL 의 파일을 삭제
     */
    void delete(String url);
}
