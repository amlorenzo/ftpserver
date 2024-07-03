package com.irg.ftpserver.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomSFTPHostBlockedEventListener {
    private static final Logger logger = LoggerFactory.getLogger(CustomSFTPHostBlockedEventListener.class);

    @EventListener
    public void handleCustomSFTPHostBlockedEvent(HostBlockedEvent event) {
        logger.info("Received CustomSFTPHostBlockedEvent at {}", LocalDateTime.now());
        logger.info("Host {} is blocked. Clossing Session: {}", event.getHost(), event.getSession());
        event.getSession().close(true);
    }
}
