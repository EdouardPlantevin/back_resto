package com.edouard.back_resto.service;


import com.edouard.back_resto.entity.Criteria;
import com.edouard.back_resto.entity.Rating;
import com.edouard.back_resto.entity.Restaurant;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.exception.RestaurantNotFoundException;
import com.edouard.back_resto.exception.UserUnauthorizeException;
import com.edouard.back_resto.model.request.RatingRequest;
import com.edouard.back_resto.repository.CriteriaRepository;
import com.edouard.back_resto.repository.RatingRepository;
import com.edouard.back_resto.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final CurrentUserService currentUserService;
    private final RatingRepository ratingRepository;
    private final RestaurantRepository restaurantRepository;
    private final CriteriaRepository criteriaRepository;


    public void create(RatingRequest ratingRequest) {

        User user = currentUserService.getCurrentUser();
        Restaurant restaurant = restaurantRepository.findById(ratingRequest.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(ratingRequest.restaurantId()));

        if (!restaurant.getSquad().getUsers().contains(user)) {
            throw new UserUnauthorizeException("Noter un restaurant qui n'appartient pas à l'une de ces squads");
        }

        Criteria criteria = criteriaRepository.findById(ratingRequest.criteriaId())
                .orElseThrow(() -> new EntityNotFoundException("Criteria with id " + ratingRequest.criteriaId() + " not found"));

        try {
            Rating rating = new Rating();
            rating.setCriteria(criteria);
            rating.setRestaurant(restaurant);
            rating.setUser(user);
            rating.setScore(ratingRequest.score());

            ratingRepository.save(rating);
        } catch (Exception e) {
            throw new RuntimeException("Error creating rating: " + e.getMessage());
        }

    }


}
