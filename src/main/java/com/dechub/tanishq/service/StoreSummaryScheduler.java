package com.dechub.tanishq.service;

import com.dechub.tanishq.config.StoreSummaryCache;
import com.dechub.tanishq.dto.eventsDto.StoreSummaryWrapperDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StoreSummaryScheduler {

    @Autowired
    private TanishqPageService tanishqPageService;

    @Autowired
    private StoreSummaryCache storeSummaryCache;

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // every 6 hours
    public void refreshCachedSummaries() {
        for (String rbm : storeSummaryCache.getCachedRbmUsernames()) {
            try {
                StoreSummaryWrapperDTO summary = tanishqPageService.fetchStoreSummariesByRbmParallel(rbm, null, null);
                storeSummaryCache.put(rbm, summary);
                System.out.println("Refreshed cache for: " + rbm);
            } catch (Exception e) {
                System.err.println("Failed to refresh RBM " + rbm + ": " + e.getMessage());
            }
        }
    }
}

