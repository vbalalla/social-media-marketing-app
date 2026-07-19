package io.serendia.ai.controller;

import io.serendia.ai.service.LlmService;
import io.serendia.ai.service.LlmService.SentimentResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class SentimentController {

    private final LlmService llmService;

    @PostMapping("/sentiment")
    public ResponseEntity<SentimentResult> analyzeSentiment(
            @RequestBody @Valid SentimentRequest request
    ) {
        SentimentResult result = llmService.analyzeSentiment(request.text());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/suggest-reply")
    public ResponseEntity<java.util.Map<String, String>> suggestReply(
            @RequestBody @Valid SentimentRequest request
    ) {
        String text = request.text();
        String suggestion = "Hi there! Thank you for reaching out. We appreciate your message: \"" + text + "\" and will get back to you shortly.";
        String lower = text.toLowerCase();
        if (lower.contains("plan") || lower.contains("pricing") || lower.contains("subscription")) {
            suggestion = "Thanks for asking! Our subscription plans start at just $29/mo. You can view all options on our pricing page: https://serendia.io/pricing.";
        } else if (lower.contains("promo") || lower.contains("discount") || lower.contains("coupon")) {
            suggestion = "We appreciate your interest! You can use the code SERENDIA15 to get 15% off your first 3 months.";
        } else if (lower.contains("support") || lower.contains("help")) {
            suggestion = "Our customer support team is available 24/7. Please let us know what issue you're facing and we will resolve it immediately!";
        }
        return ResponseEntity.ok(java.util.Map.of("reply", suggestion));
    }

    public record SentimentRequest(@NotBlank String text) {}
}
