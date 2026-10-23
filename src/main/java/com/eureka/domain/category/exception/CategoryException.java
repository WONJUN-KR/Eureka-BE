package com.eureka.domain.category.exception;

import com.eureka.global.exception.BusinessException;
import com.eureka.global.exception.ErrorCode;

public class CategoryException extends BusinessException {

    private CategoryException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static CategoryException notFound() {
        return new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }
}
