package com.irg.ftpserver.service;

import com.irg.ftpserver.config.SFTPServerConfig;
import com.irg.ftpserver.config.SFTPServerProperties;
import com.irg.ftpserver.events.HostBlockedEvent;
import com.irg.ftpserver.model.SFTPBlockedHost;
import com.irg.ftpserver.model.SFTPServerConfiguration;
import com.irg.ftpserver.model.SFTPUser;
import com.irg.ftpserver.repository.SFTPBlockedHostsRepository;
import com.irg.ftpserver.repository.SFTPServerConfigurationRepository;
import com.irg.ftpserver.repository.SFTPUserRepository;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@DependsOn({"SFTPInitialConfigService", "SFTPInitialUserInitService"})
public class SFTPLoginService implements PasswordAuthenticator, ApplicationEventPublisherAware {
    private final Logger logger = LoggerFactory.getLogger(SFTPLoginService.class);
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final int maxLoginAttempts;
    private final int delayBetweenAttempts;
    private ApplicationEventPublisher eventPublisher;
    private final SFTPUserRepository sftpUserRepository;
    private final SFTPServerConfigurationRepository sftpServerConfigurationRepository;
    private final SFTPBlockedHostsRepository sftpBlockedHostsRepository;
    private final PasswordEncoder passwordEncoder;


    public SFTPLoginService(SFTPServerProperties sftpServerProperties, SFTPUserRepository sftpUserRepository,
                            SFTPServerConfigurationRepository sftpServerConfigurationRepository,
                            SFTPBlockedHostsRepository sftpBlockedHostsRepository, PasswordEncoder passwordEncoder, SFTPServerConfig sftpServerConfig) {
        this.sftpUserRepository = sftpUserRepository;
        this.sftpServerConfigurationRepository = sftpServerConfigurationRepository;
        this.sftpBlockedHostsRepository = sftpBlockedHostsRepository;
        this.passwordEncoder = passwordEncoder;

        List<SFTPServerConfiguration> configs = sftpServerConfigurationRepository.findAll();
        if (configs.isEmpty()) {
            logger.warn("SFTP Server configuration not found, check your application.yml file");
            throw new RuntimeException("SFTP Server configuration not found");
        }
        //Returns the latest configuration from the database
        SFTPServerConfiguration sftpServerConfig1 = configs.getLast();
        this.maxLoginAttempts = sftpServerConfig1.getMaxLoginAttemptThreshold();
        this.delayBetweenAttempts = sftpServerConfig1.getDelayBetweenLoginAttempts();
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    public void recordFailedAttempt(ServerSession session){
        String ipAddress = ((InetSocketAddress) session.getIoSession().getRemoteAddress())
                .getAddress().getHostAddress();
        int attemptCount = loginAttempts.getOrDefault(ipAddress,0) + 1;
        loginAttempts.put (ipAddress, attemptCount);
        logger.info("Recorded failed attempt for IP Address: {}. Attempt count: {}", ipAddress, attemptCount);

        if (attemptCount >= maxLoginAttempts){
            String reason = String.format("Exceeded maximum login attempts %d", maxLoginAttempts);

            Date blockedAt = new Date();
            boolean notAllow = false;

            Optional<SFTPBlockedHost> existingEntry = sftpBlockedHostsRepository.findByIpAddress(ipAddress);

            if(existingEntry.isPresent()){
                SFTPBlockedHost blockedHost = existingEntry.get();
                blockedHost.setReason(reason);
                blockedHost.setBlockedAt(blockedAt);
                blockedHost.setAllow(notAllow);
                this.sftpBlockedHostsRepository.save(blockedHost);
            } else {
                SFTPBlockedHost blockedHost = new SFTPBlockedHost(null, ipAddress, reason, blockedAt, notAllow);
                this.sftpBlockedHostsRepository.save(blockedHost);
            }
            logger.warn("Blocking Host: {} after {} failed login attempts", ipAddress, attemptCount);
            loginAttempts.remove(ipAddress);
            eventPublisher.publishEvent(new HostBlockedEvent(session, ipAddress));
        }
    }

    public boolean isBlocked(String ipAddress){
        Optional<SFTPBlockedHost> blockedHost = sftpBlockedHostsRepository.findByIpAddressAndAllowFalse(ipAddress);
        return blockedHost.isPresent() && !blockedHost.get().isAllow();
    }

    public void clearAttempts(String ipAddress){
        loginAttempts.remove(ipAddress);
        logger.info("Cleared attempts for host: {}", ipAddress);
    }

    public void unblock(String ipAddress){
        Optional<SFTPBlockedHost> blockedHost = sftpBlockedHostsRepository.findByIpAddress(ipAddress);
        if (blockedHost.isPresent()){
            SFTPBlockedHost host = blockedHost.get();
            host.setAllow(true);
            sftpBlockedHostsRepository.save(host);
            logger.info("Unblocked host: {}", ipAddress);
        } else {
            logger.warn("Attempted to unblock host that was not blocked: {}", ipAddress);
        }

    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session)
            throws PasswordChangeRequiredException, AsyncAuthException {

        String ipAddress  = ((InetSocketAddress) session.getIoSession().getRemoteAddress()).getAddress()
                .getHostAddress();
        logger.info("Authenticating user: {} from host: {}", username, ipAddress);

        if (isBlocked(ipAddress)){
            logger.warn("Blocked host attempted to connect: {}", ipAddress);
            return false;
        }

        Optional<SFTPUser> sftpUser = sftpUserRepository.findByUsername(username);
        if(sftpUser.isEmpty()){
            logger.info("User not found: {}", username);
            recordFailedAttempt(session);
            return false;
        }

        SFTPUser user = sftpUser.get();
        boolean isAuthenticated = passwordEncoder.matches(password, user.getPassword());
        if (!isAuthenticated){
            logger.info("Authentication failed for user: {} From Host:{}", username,
                    session.getIoSession().getRemoteAddress());
            recordFailedAttempt(session);
            if (isBlocked(ipAddress)){
                logger.warn("Blocked IP attempted to connect after exceeding attempts: {}", ipAddress);
                return false;
            } else {
                try {
                    Thread.sleep(delayBetweenAttempts);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted while delaying between attempts for host: {}", ipAddress, e);
                }
            }
        } else {
            logger.info("Authentication succeeded for user: {} From: {}", username,
                    session.getIoSession().getRemoteAddress());
            clearAttempts(ipAddress);
        }
        return isAuthenticated;
    }
}
