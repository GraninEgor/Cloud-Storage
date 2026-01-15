package org.example.cloudstorage.core.exception;

public class StorageAccessException extends RuntimeException {
    public StorageAccessException() {
        super("Unexpected error");
    }

    public StorageAccessException(String message) {
        super(message);
    }
}
