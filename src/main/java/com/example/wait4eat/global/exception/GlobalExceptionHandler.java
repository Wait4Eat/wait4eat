package com.example.wait4eat.global.exception;

import com.example.wait4eat.global.dto.response.ErrorResponse;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.PathElementException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 기본 에러 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("Custom Exception [statusCode = {}, msg = {}]",
                e.getHttpStatus(), e.getMessage());

        return ResponseEntity.status(e.getHttpStatus())
                .body(ErrorResponse.from(e));
    }

    // Request Validation 에러 발생 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ExceptionType exceptionType = ExceptionType.REQUEST_VALIDATION_FAILED;

        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> "["+error.getField()+"] " + error.getDefaultMessage())
                .toList();

        log.error("Validation Exception [uri = {}, msg = {}]",
                e.getHeaders().getLocation(), e.getMessage());

        return ResponseEntity.status(exceptionType.getHttpStatus())
                .body(ErrorResponse.of(new CustomException(exceptionType),String.join(", ",errors)));
    }

    // Request Dto의 필드 타입과 Request Body 타입이 일치하지 않을 경우
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ExceptionType exceptionType = ExceptionType.REQUEST_FORMAT_INVALID;

        String message = "JSON 형식이 잘못되었거나, 필드 타입이 올바르지 않습니다.";
        String fieldName = null;

        // 필드 타입이 올바르지 않을 경우
        Throwable cause = e.getCause();
        if (cause instanceof JsonMappingException mappingException) {
            List<JsonMappingException.Reference> path = mappingException.getPath();
            if(!path.isEmpty()) {
                fieldName = path.get(0).getFieldName();
                message = "wrong field: " + fieldName;
            }
        }

        return ResponseEntity.status(exceptionType.getHttpStatus())
                .body(ErrorResponse.of(new CustomException(exceptionType),message));
    }

    // 권한 체크 후 AccessDeniedException 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access Denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.from(new CustomException(ExceptionType.NO_PERMISSION_ACTION)));
    }

    // 잘못된 정렬 파라미터로 인해 발생하는 예외 처리
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> invalidDataAccessApiUsageException(InvalidDataAccessApiUsageException e) {
        log.warn("InvalidDataAccessApiUsageException: {}", e.getMessage());
        ExceptionType exceptionType = ExceptionType.INVALID_PARAMETER;
        if (e.getRootCause() instanceof PathElementException) {
            exceptionType = ExceptionType.INVALID_SORT_PARAMETER;
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(new CustomException(exceptionType)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Server Exception [msg = {}, cause = {}]",
                e.getMessage(), e.getCause());

        return ResponseEntity.internalServerError()
                .body(ErrorResponse.from(new CustomException(ExceptionType.INTERNAL_SERVER_ERROR)));
    }
}
