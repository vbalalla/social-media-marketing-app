package io.serendia.ad.service;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class AdPlatformAdapterRegistry {
    private final Map<String, AdPlatformAdapter> adapters = new HashMap<>();

    public AdPlatformAdapterRegistry(List<AdPlatformAdapter> adapterList) {
        for (AdPlatformAdapter adapter : adapterList) {
            adapters.put(adapter.getPlatform().toUpperCase(), adapter);
        }
    }

    public Optional<AdPlatformAdapter> getAdapter(String platform) {
        if (platform == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(adapters.get(platform.toUpperCase()));
    }
}
