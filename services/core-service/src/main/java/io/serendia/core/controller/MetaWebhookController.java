package io.serendia.core.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/webhooks/meta")
@RequiredArgsConstructor
@Slf4j
public class MetaWebhookController {

    private final StringRedisTemplate redisTemplate;

    @Value("${meta.app-secret:placeholder_meta_app_secret}")
    private String appSecret;

    @Value("${meta.webhook-verify-token:serendia_verify_token}")
    private String verifyToken;

    /**
     * Meta webhook challenge verification (GET /webhooks/meta)
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam("hub.verify_token") String token
    ) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Meta Webhook successfully verified");
            return ResponseEntity.ok(challenge);
        }
        log.warn("Meta Webhook verification failed due to token mismatch");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Receive webhook events (POST /webhooks/meta)
     */
    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody String body,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature
    ) {
        log.info("Received Meta Webhook: {}", body);

        if (signature != null && !validateSignature(body, signature)) {
            log.warn("Invalid signature on Meta Webhook payload");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Publish raw JSON payload to Redis
        redisTemplate.convertAndSend("webhook:meta", body);
        log.info("Published webhook event to Redis channel 'webhook:meta'");

        return ResponseEntity.ok().build();
    }

    private boolean validateSignature(String body, String signature) {
        try {
            if (!signature.startsWith("sha256=")) {
                return false;
            }
            String expectedSig = signature.substring(7);
            
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            
            byte[] hash = sha256HMAC.doFinal(body.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return expectedSig.equals(hexString.toString());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error calculating HmacSHA256 for signature validation: {}", e.getMessage());
            return false;
        }
    }
}
