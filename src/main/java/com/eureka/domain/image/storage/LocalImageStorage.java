package com.eureka.domain.image.storage;

import com.eureka.domain.image.exception.ImageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * local 프로필 전용 - S3 없이 업로드 파일을 디스크에 저장하고 /uploads/** 로 서빙 (LocalUploadConfig)
 */
@Component
@Profile("local")
public class LocalImageStorage implements ImageStorage {

    public static final String URL_PREFIX = "/uploads/";

    private final Path root;

    public LocalImageStorage(@Value("${eureka.upload-dir:uploads}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String key) {
        Path target = resolve(key);
        try (InputStream in = file.getInputStream()) {
            Files.createDirectories(target.getParent());
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw ImageException.uploadFailed();
        }
        return URL_PREFIX + key;
    }

    @Override
    public void delete(String url) {
        if (!url.startsWith(URL_PREFIX)) {
            throw ImageException.invalidUrl();
        }
        try {
            Files.deleteIfExists(resolve(url.substring(URL_PREFIX.length())));
        } catch (IOException e) {
            throw ImageException.deleteFailed();
        }
    }

    public Path getRoot() {
        return root;
    }

    // key 에 ../ 가 섞여 업로드 폴더 밖으로 나가는 것 차단
    private Path resolve(String key) {
        Path target = root.resolve(key).normalize();
        if (!target.startsWith(root)) {
            throw ImageException.invalidUrl();
        }
        return target;
    }
}
