package com.spring.api.API.security.Exceptions;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
    public StorageException(String e, Throwable t){
        super(e,t);
    }
}
