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

    public record SentimentRequest(@NotBlank String text) {}
}
