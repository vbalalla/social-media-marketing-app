package io.serendia.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Component
@ConfigurationProperties(prefix = "jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

    @NotBlank
    private String privateKey;   // Base64-encoded PKCS8 DER (RSA 2048)

    @NotBlank
    private String publicKey;    // Base64-encoded X.509 DER (RSA 2048)

    @NotBlank
    private String issuer;

    @Positive
    private long accessTokenExpiryMinutes = 15;

    @Positive
    private long refreshTokenExpiryDays = 7;
}
