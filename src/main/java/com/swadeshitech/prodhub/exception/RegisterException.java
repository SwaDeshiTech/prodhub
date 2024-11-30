package com.swadeshitech.prodhub.exception;

import com.swadeshitech.prodhub.enums.ErrorCode;

public class RegisterException extends RuntimeException {
    
    private final ErrorCode errorCode;

    public RegisterException(ErrorCode errorCode){
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
