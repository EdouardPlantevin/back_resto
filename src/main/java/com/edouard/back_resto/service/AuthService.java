package com.edouard.back_resto.service;

import com.edouard.back_resto.configuration.JwtUtils;
import com.edouard.back_resto.entity.RefreshToken;
import com.edouard.back_resto.entity.User;
import com.edouard.back_resto.exception.UserAlreadyExistsException;
import com.edouard.back_resto.exception.UserInvalidException;
import com.edouard.back_resto.model.request.LoginRequest;
import com.edouard.back_resto.model.request.RegisterRequest;
import com.edouard.back_resto.model.response.AuthResponse;
import com.edouard.back_resto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;


    /**
     * Enregistre un nouvel utilisateur avec les détails d'inscription fournis. Si l'email fourni est déjà
     * associé à un utilisateur existant, une exception est levée. Le mot de passe de l'utilisateur est
     * encodé avant d'être sauvegardé. Le nouvel utilisateur créé se voit attribuer un rôle par défaut "ROLE_USER".
     *
     * @param request la requête d'inscription contenant l'email, le nom d'utilisateur et le mot de passe du nouvel utilisateur
     * @return l'objet {@code User} nouvellement créé après avoir été sauvegardé dans la base de données
     * @throws RuntimeException si l'email est déjà associé à un utilisateur existant
     */
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already taken!");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }


    /**
     * Authentifie un utilisateur en fonction de la requête de connexion fournie. En cas d'authentification réussie,
     * génère un access token et un refresh token pour l'utilisateur.
     *
     * @param request la requête de connexion contenant l'email et le mot de passe de l'utilisateur
     * @return un objet {@code AuthResponse} contenant l'access token, le refresh token, le type de token,
     *         et le temps d'expiration de l'access token
     * @throws RuntimeException si l'authentification échoue en raison d'identifiants invalides
     */
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            if (!authentication.isAuthenticated()) {
                throw new UserInvalidException(request.email());
            }

            String email = request.email();
            String accessToken = jwtUtils.generateAccessToken(email);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

            return new AuthResponse(
                    accessToken,
                    refreshToken.getToken(),
                    "Bearer",
                    jwtUtils.getAccessTokenExpiration()
            );

        } catch (AuthenticationException e) {
            log.error("Authentication error: {}", e.getMessage());
            throw new UserInvalidException(request.email());
        }
    }


    /**
     * Rafraîchit l'access token et le refresh token en utilisant la chaîne de refresh token fournie.
     * Effectue une série d'opérations incluant la vérification du refresh token, l'extraction de l'email
     * à partir du token fourni, la génération de l'access token, et la rotation du refresh token.
     *
     * @param refreshTokenString le refresh token sous forme de chaîne, utilisé pour demander un nouveau jeu de tokens
     * @return un objet {@code AuthResponse} contenant le nouvel access token, le nouveau refresh token,
     *         le type de token, et le temps d'expiration de l'access token
     * @throws RuntimeException si le refresh token est invalide, expiré ou révoqué
     */
    public AuthResponse refreshToken(String refreshTokenString) {
        refreshTokenService.verifyRefreshToken(refreshTokenString);
        String email = jwtUtils.extractEmail(refreshTokenString);
        String newAccessToken = jwtUtils.generateAccessToken(email);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(email);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer",
                jwtUtils.getAccessTokenExpiration()
        );
    }

    /**
     * Déconnecte un utilisateur en révoquant le refresh token fourni. Le refresh token est marqué
     * comme révoqué dans le système pour empêcher toute utilisation ultérieure. Cette opération garantit
     * que la session de l'utilisateur est terminée.
     *
     * @param refreshToken le refresh token associé à la session de l'utilisateur à révoquer
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}

