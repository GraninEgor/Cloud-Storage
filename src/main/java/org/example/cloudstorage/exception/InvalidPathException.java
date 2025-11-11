package org.example.cloudstorage.exception;

import org.springframework.http.HttpStatus;

public class InvalidPathException extends AppException{
    public InvalidPathException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
