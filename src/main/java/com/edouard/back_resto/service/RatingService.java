package com.edouard.back_resto.service;


import com.edouard.back_resto.entity.Criteria;
import com.edouard.back_resto.entity.Rating;
import com.edouard.back_resto.entity.Restaurant;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.model.request.RatingRequest;
import com.edouard.back_resto.repository.CriteriaRepository;
import com.edouard.back_resto.repository.RatingRepository;
import com.edouard.back_resto.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final CurrentUserService currentUserService;
    private final RatingRepository ratingRepository;
    private final RestaurantRepository restaurantRepository;
    private final CriteriaRepository criteriaRepository;


    public String create(RatingRequest ratingRequest) {

        User user = currentUserService.getCurrentUser();
        Restaurant restaurant = restaurantRepository.findById(ratingRequest.restaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getSquad().getUsers().contains(user)) {
            throw new RuntimeException("User can't rate a restaurant not in his squad");
        }

        Criteria criteria = criteriaRepository.findById(ratingRequest.criteriaId())
                .orElseThrow(() -> new RuntimeException("Criteria not found"));

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
