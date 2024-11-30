package com.swadeshitech.prodhub.exception;

import com.swadeshitech.prodhub.enums.ErrorCode;

public class DatabaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public DatabaseException(ErrorCode errorCode){
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
