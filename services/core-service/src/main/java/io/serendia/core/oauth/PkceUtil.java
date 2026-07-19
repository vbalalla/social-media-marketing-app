package io.serendia.core.oauth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PKCE (Proof Key for Code Exchange) utilities — RFC 7636.
 *
 * <p>Prevents authorization code interception attacks by binding the
 * authorization request to the token exchange.
 */
public final class PkceUtil {

    private PkceUtil() {}

    /**
     * Generates a cryptographically random code_verifier (43-128 chars, URL-safe).
     * Per RFC 7636 §4.1: 32 bytes of random data, base64url-encoded without padding.
     */
    public static String generateCodeVerifier() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Derives the code_challenge from the code_verifier using S256 method.
     * {@code code_challenge = BASE64URL(SHA256(ASCII(code_verifier)))}
     */
    public static String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
