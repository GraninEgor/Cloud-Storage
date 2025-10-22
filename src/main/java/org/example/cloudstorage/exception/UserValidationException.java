package org.example.cloudstorage.exception;

import org.springframework.http.HttpStatus;

public class UserValidationException extends AppException {
    public UserValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}