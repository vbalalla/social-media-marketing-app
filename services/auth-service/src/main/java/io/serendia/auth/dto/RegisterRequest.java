package io.serendia.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration request DTO.
 * Password policy: min 12 chars, at least one uppercase, one lowercase, one digit.
 */
public record RegisterRequest(
        @NotBlank @Email(message = "Must be a valid email address")
        String email,

        @NotBlank @Size(min = 12, message = "Password must be at least 12 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
        )
        String password,

        @NotBlank @Size(min = 2, max = 100)
        String fullName
) {}
