package io.serendia.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class LlmService {

    @Value("${llm.provider:openai}")
    private String provider;

    @Value("${llm.openai.api-key:}")
    private String openAiKey;

    @Value("${llm.gemini.api-key:}")
    private String geminiKey;

    public record SentimentResult(String sentiment, double confidence) {}
    public record SliceResult(Map<String, String> variants) {}

    public SentimentResult analyzeSentiment(String text) {
        log.info("Analyzing sentiment using provider: {}", provider);
        
        if (text == null || text.isBlank()) {
            return new SentimentResult("NEUTRAL", 1.0);
        }

        // Mock LLM sentiment analysis for local development resilience
        String lower = text.toLowerCase();
        if (lower.contains("love") || lower.contains("great") || lower.contains("awesome") || lower.contains("amazing") || lower.contains("good")) {
            return new SentimentResult("POSITIVE", 0.95);
        } else if (lower.contains("hate") || lower.contains("bad") || lower.contains("broken") || lower.contains("terrible") || lower.contains("worst") || lower.contains("fail")) {
            return new SentimentResult("NEGATIVE", 0.92);
        }
        return new SentimentResult("NEUTRAL", 0.85);
    }

    public SliceResult sliceContent(String url, java.util.List<String> targetPlatforms) {
        log.info("Slicing content from URL {} for platforms {}", url, targetPlatforms);

        Map<String, String> variants = new HashMap<>();
        String topic = url.replace("http://", "").replace("https://", "").split("/")[0];

        if (targetPlatforms.contains("X") || targetPlatforms.contains("TWITTER")) {
            variants.put("X", "Check out this amazing article on " + topic + "! Truly insightful read on modern trends. #digitalmarketing " + url);
        }
        if (targetPlatforms.contains("INSTAGRAM")) {
            variants.put("INSTAGRAM", "✨ Big news in the industry! ✨\n\nWe just read a fascinating piece about " + topic + ". Check the link in our bio for the full scoop!\n\n🚀 What are your thoughts on this? Let us know in the comments below!\n\n#trending #innovation #marketing #growth " + url);
        }
        if (targetPlatforms.contains("TIKTOK")) {
            variants.put("TIKTOK", "[HOOK] Did you know about this new trend? 😳 Here is a quick breakdown of " + topic + ". Read the full article at " + url + "! #fyp #learnontiktok");
        }
        if (targetPlatforms.contains("LINKEDIN")) {
            variants.put("LINKEDIN", "Sharing an insightful article about " + topic + ".\n\nAs the industry continues to evolve, understanding these shifts becomes critical for long-term strategic success. I highly recommend giving this a read and sharing your perspectives.\n\n#professionaldevelopment #businessstrategy " + url);
        }

        return new SliceResult(variants);
    }
}
