package com.edouard.jwt.service;

import com.edouard.jwt.entity.User;
import com.edouard.jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Localise l'utilisateur en fonction de l'email fourni. Si aucun utilisateur n'est trouvé avec l'email donné,
     * une {@code UsernameNotFoundException} est levée. Construit un objet {@code UserDetails}
     * en utilisant l'email, le mot de passe et le rôle de l'utilisateur trouvé.
     *
     * @param email l'adresse email identifiant l'utilisateur dont les données sont requises
     * @return un objet {@code UserDetails} entièrement rempli contenant les informations de l'utilisateur
     * @throws UsernameNotFoundException si aucun utilisateur n'est trouvé avec l'email spécifié
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
