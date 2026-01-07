package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.Restaurant;
import com.edouard.back_resto.entity.Squad;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.exception.RestaurantAlreadyExistException;
import com.edouard.back_resto.exception.UserUnauthorizeException;
import com.edouard.back_resto.mapper.RestaurantMapper;
import com.edouard.back_resto.model.dto.RestaurantDto;
import com.edouard.back_resto.model.request.RestaurantRequest;
import com.edouard.back_resto.repository.RestaurantRepository;
import com.edouard.back_resto.repository.SquadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SquadRepository squadRepository;
    private final CurrentUserService currentUserService;
    private final RestaurantMapper restaurantMapper;


    public List<RestaurantDto> findAll(Long squadId) {
        User user = currentUserService.getCurrentUser();
        Squad squad = squadRepository.findByIdAndUsers(squadId, Set.of(user))
                .orElseThrow(() -> new RuntimeException("Squad not found or User not in this squad: " + user.getEmail() + " " + squadId));

        return restaurantRepository.findAllBySquad(squad)
                .stream()
                .map(restaurantMapper::toDto)
                .toList();
    }

    public void create(RestaurantRequest restaurantRequest) {

        Squad squad = squadRepository.findById(restaurantRequest.squadId()).orElseThrow(() -> new RuntimeException("Squad not found"));

        if (restaurantRepository.findByNameAndSquad(restaurantRequest.name(), squad).isPresent()) {
            throw new RestaurantAlreadyExistException(restaurantRequest.name());
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setCreatedAt(new Date());
        restaurant.setName(restaurantRequest.name());
        restaurant.setAddress(restaurantRequest.address());
        restaurant.setDescription(restaurantRequest.description());
        restaurant.setPhone(restaurantRequest.phone());
        restaurant.setSquad(squad);

        restaurantRepository.save(restaurant);
    }

    public void delete(Long restaurantId) {
        User user = currentUserService.getCurrentUser();

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        Squad squad = restaurant.getSquad();

        if (!squad.getUsers().contains(user)) {
            throw new UserUnauthorizeException("Supprimer ce restaurant");
        }

        restaurantRepository.delete(restaurant);
    }

}
