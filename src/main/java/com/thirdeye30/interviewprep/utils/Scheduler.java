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
    
    private volatile boolean isWorking = false;
    private final ExplorerService explorerService;
    
    @Scheduled(fixedDelay = 120000)
    public void uploadStaleDocument() {
        try {
            explorerService.updateActionInDatabase();
        } catch (Exception e) {
            log.error("Unexpected error occurred during scheduled action update", e);
        }
    }
    
    @Scheduled(fixedRateString = "${thirdeye.message.broker.read.rate}")
    public void readMessagesFromMessageBroker() {
        synchronized (this) {
            if (isWorking) {
                return; 
            }
            isWorking = true;
        }
        try {
        	explorerService.createCourses();
            log.info("Starting to read message from broker...");
        } catch (Exception e) {
            log.error("Error occurred while reading messages: ", e);
        } finally {
            isWorking = false;
        }
    }
}