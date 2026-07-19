package io.serendia.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM symmetric encryption for OAuth tokens at rest.
 *
 * <p>Encrypted format: {@code Base64(iv) + "." + Base64(ciphertext+authTag)}
 *
 * <p>The master key is read from the TOKEN_ENCRYPTION_MASTER_KEY environment variable
 * (32 hex-encoded bytes = 64 hex characters). In production, this key itself should be
 * managed by AWS Secrets Manager and rotated periodically.
 */
@Service
@Slf4j
public class TokenEncryptionService {

    private static final String ALGORITHM  = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LEN = 12;    // 96-bit IV recommended for GCM
    private static final int    GCM_TAG_LEN = 128;  // 128-bit authentication tag

    private final SecretKey secretKey;

    public TokenEncryptionService(@Value("${token.encryption.master-key}") String masterKeyHex) {
        byte[] keyBytes = hexToBytes(masterKeyHex);
        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                "TOKEN_ENCRYPTION_MASTER_KEY must be exactly 64 hex characters (32 bytes for AES-256)");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LEN];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LEN, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            return Base64.getUrlEncoder().withoutPadding().encodeToString(iv)
                    + "."
                    + Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedValue) {
        try {
            String[] parts = encryptedValue.split("\\.", 2);
            if (parts.length != 2) throw new IllegalArgumentException("Invalid encrypted token format");

            byte[] iv         = Base64.getUrlDecoder().decode(parts[0]);
            byte[] ciphertext = Base64.getUrlDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LEN, iv));
            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed — token may be tampered or key rotated", e);
        }
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
