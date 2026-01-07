package com.edouard.back_resto.exception;

public class UserNotAuthenticatedException extends RuntimeException {
    public UserNotAuthenticatedException() {
        super("No authenticated user found");
    }
}
