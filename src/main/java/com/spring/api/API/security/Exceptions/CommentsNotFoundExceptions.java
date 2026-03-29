package com.spring.api.API.security.Exceptions;

public class CommentsNotFoundExceptions extends RuntimeException {
    public CommentsNotFoundExceptions(String e){
        super(e);
    }
}
