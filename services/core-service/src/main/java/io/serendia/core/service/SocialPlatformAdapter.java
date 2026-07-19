package io.serendia.core.service;

import io.serendia.core.domain.SocialAccountEntity;
import io.serendia.core.domain.SocialPlatform;

import java.util.List;

public interface SocialPlatformAdapter {
    SocialPlatform getPlatform();
    String publishPost(SocialAccountEntity account, String content, List<String> mediaUrls);
}
