package com.eureka.domain.region.exception;

import com.eureka.global.exception.BusinessException;
import com.eureka.global.exception.ErrorCode;

public class RegionException extends BusinessException {

    private RegionException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static RegionException notFound() {
        return new RegionException(RegionErrorCode.REGION_NOT_FOUND);
    }
}
