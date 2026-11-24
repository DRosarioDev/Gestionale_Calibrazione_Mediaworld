package it.drosariodev.calibrazione.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Genera e verifica il Bearer Token.
 * La chiave e la durata arrivano da application.properties
 * (app.jwt.secret, app.jwt.expiration-hours) e mai dal codice.
 */
@Component
@Slf4j
public class JWTUtil {

    private static final String ISSUER = "calibrazione.it";
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-hours:24}")
    private long expirationHours;

    private Algorithm algorithm;

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank() || secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "app.jwt.secret non valido: serve una chiave di almeno "
                            + MIN_SECRET_LENGTH + " caratteri. Impostala in application.properties "
                            + "oppure con la variabile d'ambiente JWT_SECRET.");
        }
        algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(String email) {
        try {
            Instant now = Instant.now();
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(email)
                    .withIssuedAt(now)
                    .withExpiresAt(now.plus(expirationHours, ChronoUnit.HOURS))
                    .sign(algorithm);
        } catch (JWTCreationException ex) {
            log.warn("Token not valid {}", ex.getMessage(), ex);
            throw new IllegalArgumentException("Token not valid:" + ex);
        }
    }

    public String verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getSubject();
        } catch (JWTVerificationException ex) {
            log.warn("Token not valid {}", ex.getMessage(), ex);
            throw new IllegalArgumentException("Token not valid:" + ex);
        }
    }

}
