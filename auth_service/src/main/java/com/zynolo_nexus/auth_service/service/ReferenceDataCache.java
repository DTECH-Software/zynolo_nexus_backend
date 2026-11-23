package com.zynolo_nexus.auth_service.service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zynolo_nexus.auth_service.dto.response.ReferenceDataDto;

@Component
public class ReferenceDataCache {

    private static final long DEFAULT_TTL_SECONDS = 300;

    private final long ttlMillis;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ReferenceDataCache(@Value("${auth.reference.cache.ttl-seconds:" + DEFAULT_TTL_SECONDS + "}") long ttlSeconds) {
        this.ttlMillis = Duration.ofSeconds(ttlSeconds).toMillis();
    }

    public Optional<ReferenceDataDto> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.value);
    }

    public void put(String key, ReferenceDataDto value) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMillis));
    }

    private record CacheEntry(ReferenceDataDto value, long expiresAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
