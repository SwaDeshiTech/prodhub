package com.swadeshitech.prodhub.exception;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(DatabaseException.class)
    protected ResponseEntity<Response> handleDatabaseException(DatabaseException databaseException){
        return responseEntityBuilder(databaseException.getErrorCode());
    }

    @ExceptionHandler(RegisterException.class)
    protected ResponseEntity<Response> handleRegisterException(RegisterException registerException) {
        return responseEntityBuilder(registerException.getErrorCode());
    }

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<Response> handleCustomException(CustomException customException) {
        return responseEntityBuilder(customException.getErrorCode());
    }

    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Error methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        return processFieldErrors(fieldErrors);
    }

    private Error processFieldErrors(List<org.springframework.validation.FieldError> fieldErrors) {
        Error error = new Error(BAD_REQUEST.value(), "Validation failed");
        for (org.springframework.validation.FieldError fieldError: fieldErrors) {
            error.addFieldError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return error;
    }

    static class Error {
        private final int status;
        private final String message;
        private List<FieldError> fieldErrors = new ArrayList<>();

        Error(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public void addFieldError(String path, String message) {
            FieldError error = new FieldError("", path, message);
            fieldErrors.add(error);
        }

        public List<FieldError> getFieldErrors() {
            return fieldErrors;
        }
    }

    private ResponseEntity<Response> responseEntityBuilder(ErrorCode errorCode){
        return new ResponseEntity<>(Response.builder()
                .errorCode(errorCode.getCode())
                .httpStatus(errorCode.getStatus())
                .message(errorCode.getDescription())
                .build(),
                errorCode.getStatus());
    }
}
