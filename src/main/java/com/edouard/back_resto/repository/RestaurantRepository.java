package com.edouard.back_resto.repository;

import com.edouard.back_resto.entity.Restaurant;
import com.edouard.back_resto.entity.Squad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByNameAndSquad(String name, Squad squad);

    List<Restaurant> findAllBySquad(Squad squad);
}
