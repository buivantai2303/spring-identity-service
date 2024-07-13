package com.ketealare.identityService.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Error!", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid Message Key!", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User already exists!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "User not exists!", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1004, "User must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1005, "Password must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You dont have permission!", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min} years old", HttpStatus.FORBIDDEN),

    ;

    int code;
    String message;
    HttpStatusCode statusCode;
}
