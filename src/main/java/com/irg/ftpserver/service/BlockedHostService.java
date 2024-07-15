package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPBlockedHost;
import com.irg.ftpserver.repository.SFTPBlockedHostsRepository;
import com.irg.ftpserver.events.HostBlockedEvent;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NonNull;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing blocked hosts.
 */
@Service
@Data
@DependsOn({"SFTPInitialConfigService"})
public class BlockedHostService implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(BlockedHostService.class);

    private final SFTPBlockedHostsRepository sftpBlockedHostsRepository;

    private ApplicationEventPublisher eventPublisher;

    private final SFTPConfigurationService sftpConfigurationService;

    private final Map<String, Integer> userLookupAttempts = new ConcurrentHashMap<>();

    private final Map<String, Integer> passwordAttempts = new ConcurrentHashMap<>();

    private int maxLoginAttempts;

    private int delayBetweenAttempts;

    public BlockedHostService(SFTPBlockedHostsRepository sftpBlockedHostsRepository,
                              SFTPConfigurationService sftpConfigurationService) {
        this.sftpBlockedHostsRepository = sftpBlockedHostsRepository;
        this.sftpConfigurationService = sftpConfigurationService;
    }

    @PostConstruct
    private void init() {
        if (sftpConfigurationService != null) {
            maxLoginAttempts = sftpConfigurationService.getLatestConfiguration().getMaxLoginAttemptThreshold();
            delayBetweenAttempts = sftpConfigurationService.getLatestConfiguration().getDelayBetweenLoginAttempts();
            logger.info("Initialized BlockedHostService with maxLoginAttempts: {} and delayBetweenAttempts: {}",
                    maxLoginAttempts, delayBetweenAttempts);
        } else {
            logger.error("SFTPConfigurationService is null. Initialization failed.");
        }
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Records a failed user lookup attempt.
     *
     * @param ipAddress     the IP address of the host
     * @param reason        the reason for the block
     * @param callingMethod the method calling this function
     * @param session       the server session
     */
    public void recordFailedUserAttempt(String ipAddress, String reason, String callingMethod,
                                        ServerSession session) {
        int attemptCount = userLookupAttempts.getOrDefault(ipAddress, 0) + 1;
        userLookupAttempts.put(ipAddress, attemptCount);
        logger.info("{} - Recorded failed user lookup attempt from IP Address: {}, Attempt count: {}",
                callingMethod, ipAddress, attemptCount);

        if (attemptCount >= maxLoginAttempts) {
            blockHost(ipAddress, reason, callingMethod, session);
        }

        // Apply delay between attempts
        applyDelayBetweenAttempts();
    }

    /**
     * Records a failed password attempt.
     *
     * @param ipAddress     the IP address of the host
     * @param reason        the reason for the block
     * @param callingMethod the method calling this function
     * @param session       the server session
     */
    public void recordFailedPasswordAttempt(String ipAddress, String reason, String callingMethod,
                                            ServerSession session) {
        int attemptCount = passwordAttempts.getOrDefault(ipAddress, 0) + 1;
        passwordAttempts.put(ipAddress, attemptCount);
        logger.info("{} - Recorded failed password attempt from IP Address: {}, Attempt count: {}",
                callingMethod, ipAddress, attemptCount);

        if (attemptCount >= maxLoginAttempts) {
            blockHost(ipAddress, reason, callingMethod, session);
        }

        // Apply delay between attempts
        applyDelayBetweenAttempts();
    }

    /**
     * Blocks the host after exceeding maximum login attempts.
     *
     * @param ipAddress     the IP address of the host
     * @param reason        the reason for the block
     * @param callingMethod the method calling this function
     * @param session       the server session
     */
    private void blockHost(String ipAddress, String reason, String callingMethod, ServerSession session) {
        logger.warn("{} - Blocking IP Address: {} due to exceeded maximum login attempts: {}",
                callingMethod, ipAddress, maxLoginAttempts);
        userLookupAttempts.remove(ipAddress);
        passwordAttempts.remove(ipAddress);

        Date blockedAt = new Date();
        boolean notAllow = false;
        Optional<SFTPBlockedHost> existingEntry = sftpBlockedHostsRepository.findByIpAddress(ipAddress);

        SFTPBlockedHost blockedHost = existingEntry
                .map(host -> {
                    host.setReason(reason);
                    host.setBlockedAt(blockedAt);
                    host.setAllow(notAllow);
                    return host;
                })
                .orElseGet(() -> new SFTPBlockedHost(null, ipAddress, reason, blockedAt, notAllow));

        sftpBlockedHostsRepository.save(blockedHost);
        eventPublisher.publishEvent(new HostBlockedEvent(session, ipAddress)); // Publish event with session
    }

    /**
     * Clears the login attempts for a given IP address.
     *
     * @param ipAddress     the IP address of the host
     * @param callingMethod the method calling this function
     */
    public void clearAttempts(String ipAddress, String callingMethod) {
        userLookupAttempts.remove(ipAddress);
        passwordAttempts.remove(ipAddress);
        logger.info("{} - Cleared attempts for host: {}", callingMethod, ipAddress);
    }

    /**
     * Checks if a given IP address is blocked.
     *
     * @param ipAddress the IP address of the host
     * @return true if the host is blocked, false otherwise
     */
    public boolean isBlocked(String ipAddress) {
        Optional<SFTPBlockedHost> blockedHost = sftpBlockedHostsRepository.findByIpAddress(ipAddress);
        return blockedHost.isPresent() && !blockedHost.get().isAllow();
    }

    /**
     * Applies a delay between login attempts.
     */
    private void applyDelayBetweenAttempts() {
        try {
            Thread.sleep(delayBetweenAttempts);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while delaying between attempts", e);
        }
    }
}