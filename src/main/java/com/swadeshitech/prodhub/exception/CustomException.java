package com.swadeshitech.prodhub.exception;

import com.swadeshitech.prodhub.enums.ErrorCode;

public class CustomException extends RuntimeException {
    
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode){
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
