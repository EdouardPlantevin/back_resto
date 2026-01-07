package com.edouard.back_resto.exception;

public class UserInvalidException extends RuntimeException {
    public UserInvalidException(String message) {
      super("Invalid username or password : " + message);
    }
}
