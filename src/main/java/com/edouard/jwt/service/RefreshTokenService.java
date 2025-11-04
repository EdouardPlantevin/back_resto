package com.edouard.jwt.service;

import com.edouard.jwt.configuration.JwtUtils;
import com.edouard.jwt.entity.RefreshToken;
import com.edouard.jwt.entity.User;
import com.edouard.jwt.repository.RefreshTokenRepository;
import com.edouard.jwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Value("${app.refresh-token-expiration}")
    private long refreshTokenExpiration;


    /**
     * Crée un nouveau refresh token pour l'email de l'utilisateur spécifié, invalidant tous les tokens existants pour cet utilisateur.
     *
     * @param email l'email de l'utilisateur pour lequel le refresh token doit être créé
     * @return l'entité {@link RefreshToken} nouvellement créée
     * @throws RuntimeException si aucun utilisateur n'est trouvé avec l'email spécifié
     */
    @Transactional
    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        refreshTokenRepository.deleteByUser(user);
        String token = jwtUtils.generateRefreshToken(email);
        Instant expiryDate = Instant.now().plusSeconds(refreshTokenExpiration);

        RefreshToken refreshToken = new RefreshToken(token, user, expiryDate);
        return refreshTokenRepository.save(refreshToken);
    }


    /**
     * Vérifie la validité d'un refresh token donné. La méthode vérifie si le token existe, n'est pas expiré,
     * n'est pas révoqué, et est un refresh token JWT valide. Si le token échoue à l'une de ces vérifications,
     * la méthode supprime le token et lève une exception runtime.
     *
     * @param token le refresh token à vérifier
     * @return l'entité {@link RefreshToken} si le token est valide
     * @throws RuntimeException si le token n'est pas trouvé, est expiré, est révoqué ou est invalide
     */
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        if (!jwtUtils.validateToken(token) || !jwtUtils.isRefreshToken(token)) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Invalid refresh token");
        }

        return refreshToken;
    }

    /**
     * Révoque un refresh token en le marquant comme révoqué. Si le token fourni existe
     * dans la base de données, son statut révoqué sera défini sur true et sauvegardé. Si le token
     * n'existe pas, aucune action n'est entreprise.
     *
     * @param token le refresh token à révoquer
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken.isPresent()) {
            RefreshToken rt = refreshToken.get();
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        }
    }
}

