package org.example.cloudstorage.exception;

import org.springframework.http.HttpStatus;

public class ObjectNotFoundException extends AppException {
  public ObjectNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
