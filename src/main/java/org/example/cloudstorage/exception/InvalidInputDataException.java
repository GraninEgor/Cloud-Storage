package org.example.cloudstorage.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputDataException extends AppException{
    public InvalidInputDataException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
