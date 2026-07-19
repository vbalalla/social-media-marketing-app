package io.serendia.core.service;

import io.serendia.core.domain.SocialAccountEntity;
import io.serendia.core.domain.SocialPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TwitterXAdapter implements SocialPlatformAdapter {
    @Override
    public SocialPlatform getPlatform() {
        return SocialPlatform.X;
    }

    @Override
    public String publishPost(SocialAccountEntity account, String content, List<String> mediaUrls) {
        log.info("TwitterXAdapter publishing to X account {}: content='{}', media={}", account.getPlatformUserId(), content, mediaUrls);
        return "x_post_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
