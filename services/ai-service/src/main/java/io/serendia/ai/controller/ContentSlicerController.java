package io.serendia.ai.controller;

import io.serendia.ai.service.LlmService;
import io.serendia.ai.service.LlmService.SliceResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ContentSlicerController {

    private final LlmService llmService;

    @PostMapping("/content-slicer")
    public ResponseEntity<SliceResult> sliceContent(
            @RequestBody @Valid ContentSlicerRequest request
    ) {
        SliceResult result = llmService.sliceContent(request.url(), request.targetPlatforms());
        return ResponseEntity.ok(result);
    }

    public record ContentSlicerRequest(
            @NotBlank String url,
            @NotEmpty List<String> targetPlatforms
    ) {}
}
