package org.example.cloudstorage.exception;

import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends AppException {
    public UsernameAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}