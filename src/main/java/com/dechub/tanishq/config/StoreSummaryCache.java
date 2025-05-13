package com.dechub.tanishq.config;

import com.dechub.tanishq.dto.eventsDto.StoreSummaryWrapperDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StoreSummaryCache {

    private final Map<String, StoreSummaryWrapperDTO> cache = new ConcurrentHashMap<>();

    public StoreSummaryWrapperDTO get(String rbmUsername) {
        return cache.get(rbmUsername);
    }

    public void put(String rbmUsername, StoreSummaryWrapperDTO summary) {
        cache.put(rbmUsername, summary);
    }

    public Set<String> getCachedRbmUsernames() {
        return cache.keySet();
    }

    public void clear() {
        cache.clear();
    }
}

