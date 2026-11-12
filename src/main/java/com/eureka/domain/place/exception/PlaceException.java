package com.eureka.domain.place.exception;

import com.eureka.global.exception.BusinessException;
import com.eureka.global.exception.ErrorCode;

public class PlaceException extends BusinessException {

    private PlaceException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static PlaceException notFound() {
        return new PlaceException(PlaceErrorCode.PLACE_NOT_FOUND);
    }

    public static PlaceException required() {
        return new PlaceException(PlaceErrorCode.PLACE_REQUIRED);
    }

    public static PlaceException maxCountExceeded() {
        return new PlaceException(PlaceErrorCode.MAX_PLACE_COUNT_EXCEEDED);
    }

    public static PlaceException maxImageCountExceeded() {
        return new PlaceException(PlaceErrorCode.MAX_PLACE_IMAGE_COUNT_EXCEEDED);
    }
}
