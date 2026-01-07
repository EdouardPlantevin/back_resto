package com.edouard.back_resto.exception;

public class RestaurantAlreadyExistException extends RuntimeException {
    public RestaurantAlreadyExistException(String message) {
        super("Restaurant already exists : " + message);
    }
}
