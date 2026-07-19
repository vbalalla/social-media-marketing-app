package io.serendia.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.serendia.auth.config.JwtProperties;
import io.serendia.auth.domain.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Handles JWT issuance (RS256) and verification.
 * The private key is only available in auth-service.
 * All other services only need the public key for verification.
 */
@Service
@Slf4j
public class JwtService {

    private static final String CLAIM_USER_ID   = "uid";
    private static final String CLAIM_ROLE      = "role";
    private static final String CLAIM_EMAIL     = "email";

    private final Algorithm algorithm;
    private final RSAPublicKey publicKey;
    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            // Private key — PKCS8 DER, base64-encoded
            byte[] privBytes = Base64.getDecoder().decode(stripPemHeaders(props.getPrivateKey()));
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));

            // Public key — X.509 DER, base64-encoded
            byte[] pubBytes = Base64.getDecoder().decode(stripPemHeaders(props.getPublicKey()));
            this.publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(pubBytes));

            this.algorithm = Algorithm.RSA256(this.publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JWT RSA keys — check JWT_PRIVATE_KEY and JWT_PUBLIC_KEY env vars", e);
        }
    }

    /**
     * Issues a short-lived access token (15 min by default).
     */
    public String issueAccessToken(UserEntity user) {
        Instant now = Instant.now();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withIssuer(props.getIssuer())
                .withAudience("serendia-api")
                .withSubject(user.getId().toString())
                .withClaim(CLAIM_USER_ID, user.getId().toString())
                .withClaim(CLAIM_ROLE, user.getRole().name())
                .withClaim(CLAIM_EMAIL, user.getEmail())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(props.getAccessTokenExpiryMinutes(), ChronoUnit.MINUTES)))
                .sign(algorithm);
    }

    /**
     * Verifies a JWT access token and returns the decoded claims.
     * Throws {@link JWTVerificationException} on any validation failure.
     */
    public DecodedJWT verifyAccessToken(String token) {
        return JWT.require(algorithm)
                .withIssuer(props.getIssuer())
                .withAudience("serendia-api")
                .build()
                .verify(token);
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    /** Strips PEM header/footer lines and whitespace for raw base64 parsing. */
    private static String stripPemHeaders(String pem) {
        return pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }
}
