package com.edouard.back_resto.service;

import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.exception.UserNotAuthenticatedException;
import com.edouard.back_resto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service permettant de récupérer l'utilisateur actuellement authentifié.
 * Utilise le SecurityContext pour obtenir l'email de l'utilisateur authentifié
 * et charge l'entité User correspondante depuis la base de données.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    /**
     * Récupère l'utilisateur actuellement authentifié.
     * 
     * @return l'entité User de l'utilisateur authentifié
     * @throws RuntimeException si aucun utilisateur n'est authentifié ou si l'utilisateur n'est pas trouvé
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException();
        }
        
        String email = authentication.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Récupère l'email de l'utilisateur actuellement authentifié.
     * 
     * @return l'email de l'utilisateur authentifié
     * @throws RuntimeException si aucun utilisateur n'est authentifié
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException();
        }
        
        return authentication.getName();
    }
}

