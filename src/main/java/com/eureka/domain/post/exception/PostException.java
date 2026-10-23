package com.eureka.domain.post.exception;

import com.eureka.global.exception.BusinessException;
import com.eureka.global.exception.ErrorCode;

public class PostException extends BusinessException {

    private PostException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static PostException notFound() {
        return new PostException(PostErrorCode.POST_NOT_FOUND);
    }

    public static PostException unauthorized() {
        return new PostException(PostErrorCode.UNAUTHORIZED);
    }
}
