package com.edouard.back_resto.exception;

public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(Long restaurantId) {
        super("Restaurant with id " + restaurantId + " not found");
    }
}
