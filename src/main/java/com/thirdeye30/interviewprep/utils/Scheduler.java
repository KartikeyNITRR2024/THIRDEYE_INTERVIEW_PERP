package com.thirdeye30.interviewprep.utils;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.thirdeye30.interviewprep.services.ExplorerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {
    
    private final ExplorerService explorerService;
    
    @Scheduled(fixedDelay = 120000)
    public void uploadStaleDocument() {
        try {
            explorerService.updateActionInDatabase();
        } catch (Exception e) {
            log.error("Unexpected error occurred during scheduled action update", e);
        }
    }
}