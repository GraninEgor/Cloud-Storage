package org.example.cloudstorage.exception;

import org.springframework.http.HttpStatus;

public class UserIsNotAuthenticated extends AppException {
    public UserIsNotAuthenticated(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

    public UserIsNotAuthenticated() {
        super(HttpStatus.UNAUTHORIZED, "unauthorized");
    }
}
