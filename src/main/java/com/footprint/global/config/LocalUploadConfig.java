package com.footprint.global.config;

import com.footprint.domain.image.storage.LocalImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * local 프로필 전용 - LocalImageStorage 가 저장한 파일을 /uploads/** 로 서빙
 */
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class LocalUploadConfig implements WebMvcConfigurer {

    private final LocalImageStorage localImageStorage;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(LocalImageStorage.URL_PREFIX + "**")
                .addResourceLocations(localImageStorage.getRoot().toUri().toString());
    }
}
