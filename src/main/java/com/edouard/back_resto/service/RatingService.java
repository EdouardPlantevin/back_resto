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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final CurrentUserService currentUserService;
    private final RatingRepository ratingRepository;
    private final RestaurantRepository restaurantRepository;
    private final CriteriaRepository criteriaRepository;


    @Transactional
    public void createOrUpdate(List<RatingRequest> ratingRequests) {

        //1. Garde-fou : List vide
        if(ratingRequests == null || ratingRequests.isEmpty()) {
            return;
        }

        User user = currentUserService.getCurrentUser();
        Long restaurantId = ratingRequests.getFirst().restaurantId();

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        // 2. Vérification sécurité
        if (!restaurant.getSquad().getUsers().contains(user)) {
            throw new UserUnauthorizeException("Vous ne faites pas partie de la squad de ce restaurant");
        }

        // 3. Récupérer tous les IDs des critères demandés
        Set<Long> criteriaIds = ratingRequests.stream()
                .map(RatingRequest::criteriaId)
                .collect(Collectors.toSet());

        // 4. Batch SELECT des critères (1 seule requête) et transformation en Map
        Map<Long, Criteria> criteriaMap = criteriaRepository.findAllById(criteriaIds).stream()
                .collect(Collectors.toMap(Criteria::getId, Function.identity()));

        // 5. Batch SELECT des ratings existants pour ce user/resto (1 seule requête)
        List<Rating> existingRatingList = ratingRepository.findByRestaurantAndUser(restaurant, user);

        // On transforme en Map <CriteriaId, Rating> pour un accès instantané
        Map<Long, Rating> existingRatingMap = existingRatingList.stream()
                .collect(Collectors.toMap(r -> r.getCriteria().getId(), Function.identity()));

        List<Rating> ratingsToSave = new ArrayList<>();

        // 6. Traitement en mémoire (Pas d'appel en BD)
        for (RatingRequest req : ratingRequests) {
            Criteria criteria = criteriaMap.get(req.criteriaId());
            if (criteria == null) {
                throw new EntityNotFoundException("Critéria not found: " + req.criteriaId());
            }

            //On regarde dans notre Map si le rating existe déjà
            Rating rating = existingRatingMap.get(req.criteriaId());

            if (rating != null) {
                //Mise à jour
                rating.setScore(req.score());
            } else {
                //Création
                rating = new Rating();
                rating.setRestaurant(restaurant);
                rating.setUser(user);
                rating.setCriteria(criteria);
                rating.setScore(req.score());
            }
            ratingsToSave.add(rating);

            // 7. Batch INSERT / UPDATE final
            ratingRepository.saveAll(ratingsToSave);
        }
    }
}
