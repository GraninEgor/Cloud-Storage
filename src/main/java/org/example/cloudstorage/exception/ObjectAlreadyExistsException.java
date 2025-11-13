package org.example.cloudstorage.exception;

import org.springframework.http.HttpStatus;

public class ObjectAlreadyExistsException extends AppException {
    public ObjectAlreadyExistsException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
