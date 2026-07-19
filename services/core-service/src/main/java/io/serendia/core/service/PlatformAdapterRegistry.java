package io.serendia.core.service;

import io.serendia.core.domain.SocialPlatform;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PlatformAdapterRegistry {

    private final Map<SocialPlatform, SocialPlatformAdapter> adapters;

    public PlatformAdapterRegistry(List<SocialPlatformAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(SocialPlatformAdapter::getPlatform, a -> a));
    }

    public Optional<SocialPlatformAdapter> getAdapter(SocialPlatform platform) {
        return Optional.ofNullable(adapters.get(platform));
    }
}
