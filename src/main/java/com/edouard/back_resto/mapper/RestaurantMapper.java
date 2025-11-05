package com.edouard.back_resto.mapper;

import com.edouard.back_resto.entity.Restaurant;
import com.edouard.back_resto.model.dto.RestaurantDto;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {
    public RestaurantDto toDto(Restaurant restaurant) {
        return new RestaurantDto(
                restaurant.getId(),
                restaurant.getCreatedAt(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getDescription(),
                restaurant.getPhone()
        );
    }
}