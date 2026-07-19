package io.serendia.inbox.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serendia.inbox.domain.InboxMessageEntity;
import io.serendia.inbox.domain.InboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageIngestionService {

    private final InboxMessageRepository inboxMessageRepository;
    private final ObjectMapper           objectMapper;

    /**
     * Called by Redis MessageListenerAdapter when a message is published to 'webhook:meta'
     */
    @Transactional
    public void handleMessage(String messageJson) {
        log.info("Ingesting message from Redis: {}", messageJson);
        try {
            JsonNode root = objectMapper.readTree(messageJson);
            
            // Extract entry array
            JsonNode entryNode = root.path("entry");
            if (!entryNode.isArray() || entryNode.isEmpty()) {
                log.warn("Webhook payload does not contain an entry array");
                return;
            }

            JsonNode entry = entryNode.get(0);
            String pageId = entry.path("id").asText("unknown_page");

            // Meta payload can be a message event
            JsonNode messagingNode = entry.path("messaging");
            if (messagingNode.isArray() && !messagingNode.isEmpty()) {
                JsonNode messaging = messagingNode.get(0);
                String senderId = messaging.path("sender").path("id").asText();
                String messageId = messaging.path("message").path("mid").asText();
                String text = messaging.path("message").path("text").asText();

                if (senderId == null || senderId.isBlank() || messageId == null || messageId.isBlank()) {
                    log.warn("Messaging payload is missing sender.id or message.mid");
                    return;
                }

                ingest(pageId, "FACEBOOK", messageId, senderId, "Meta User " + senderId, text);
            }
        } catch (Exception e) {
            log.error("Failed to parse Meta webhook JSON: {}", e.getMessage(), e);
        }
    }

    private void ingest(String pageId, String platform, String platformMessageId, String senderId, String senderName, String content) {
        if (inboxMessageRepository.existsByPlatformAndPlatformMessageId(platform, platformMessageId)) {
            log.debug("Message already exists: {} - {}", platform, platformMessageId);
            return;
        }

        // Generate a stable workspace ID from the social platform page ID
        UUID workspaceId = UUID.nameUUIDFromBytes(pageId.getBytes());

        InboxMessageEntity entity = InboxMessageEntity.builder()
                .workspaceId(workspaceId)
                .platform(platform)
                .platformMessageId(platformMessageId)
                .senderId(senderId)
                .senderName(senderName)
                .content(content)
                .sentiment("NEUTRAL") // To be enriched by AI Service
                .status("UNREAD")
                .labels(List.of())
                .receivedAt(Instant.now())
                .build();

        InboxMessageEntity saved = inboxMessageRepository.save(entity);
        log.info("Successfully ingested inbox message {} for workspace {}", saved.getId(), workspaceId);
    }
}
