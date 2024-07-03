package com.irg.ftpserver.service;

import com.irg.ftpserver.config.SFTPServerProperties;
import com.irg.ftpserver.events.HostBlockedEvent;
import com.irg.ftpserver.model.User;
import lombok.NonNull;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SFTPLoginService implements PasswordAuthenticator, ApplicationEventPublisherAware {
    private final Logger logger = LoggerFactory.getLogger(SFTPLoginService.class);
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, String> userCredentials = new ConcurrentHashMap<>();
    private final Set<String> blockedHosts = ConcurrentHashMap.newKeySet();
    private final int maxLoginAttempts;
    private final long delayBetweenAttempts;
    private ApplicationEventPublisher eventPublisher;

    /***
     * User authentication should never be handled in clear text or stored in clear text this is just for dev
     * purposes right now, eventually the only auth allowed will be publickey auth, but for testing now we load
     * the user credentials from the properties file
     ***/
    public SFTPLoginService(SFTPServerProperties sftpServerProperties) {
        this.maxLoginAttempts = sftpServerProperties.getMaxLoginAttemptThreshold();
        this.delayBetweenAttempts = sftpServerProperties.getDelayBetweenLoginAttempts();
        for(User user: sftpServerProperties.getUsers()){
            userCredentials.put(user.getUsername(), user.getPassword());
        }
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    public void recordFailedAttempt(String host, ServerSession session){
        int attemptCount = loginAttempts.getOrDefault(host,0) + 1;
        loginAttempts.put (host, attemptCount);
        logger.info("Recorded failed attempt for host: {}. Attempt count: {}", host, attemptCount);
        if (attemptCount >= maxLoginAttempts){
            blockedHosts.add(host);
            logger.warn("Blocking Host: {} after {} failed login attempts", host, attemptCount);
            eventPublisher.publishEvent(new HostBlockedEvent(session, host));
        }
    }

    public boolean isBlocked(String host){
        return blockedHosts.contains(host);
    }

    public void clearAttempts(String host){
        loginAttempts.remove(host);
        logger.info("Cleared attempts for host: {}", host);
    }

    public void unblock(String host){
        blockedHosts.remove(host);
        logger.info("Unblocked host: {}", host);
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session)
            throws PasswordChangeRequiredException, AsyncAuthException {

        String host = ((InetSocketAddress) session.getIoSession().getRemoteAddress()).getAddress().getHostAddress();
        logger.info("Authenticating user: {} from host: {}", username, host);

        if (isBlocked(host)){
            logger.warn("Blocked host attempted to connect: {}", host);
            return false;
        }

        boolean isAuthenticated = userCredentials.containsKey(username) && userCredentials.get(username)
                .equals(password);
        if (!isAuthenticated){
            logger.info("Authentication failed for user: {} From Host:{}", username,
                    session.getIoSession().getRemoteAddress());
            recordFailedAttempt(host, session);
            if (isBlocked(host)){
                logger.warn("Blocked IP attempted to connect after exceeding attempts: {}", host);
                return false;
            } else {
                try {
                    Thread.sleep(delayBetweenAttempts);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted while delaying between attempts for host: {}", host, e);
                }
            }
        } else {
            logger.info("Authentication succeeded for user: {} From: {}", username,
                    session.getIoSession().getRemoteAddress());
            clearAttempts(host);
        }

        return isAuthenticated;
    }
}
