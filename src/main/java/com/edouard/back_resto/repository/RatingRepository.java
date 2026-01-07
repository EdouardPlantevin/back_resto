package com.edouard.back_resto.repository;

import com.edouard.back_resto.entity.Criteria;
import com.edouard.back_resto.entity.Rating;
import com.edouard.back_resto.entity.Restaurant;
import com.edouard.back_resto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRestaurantAndUser(Restaurant restaurant, User user);
}
