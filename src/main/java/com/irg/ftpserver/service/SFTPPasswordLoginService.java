package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPUser;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * Service for managing password-based login for SFTP.
 */
@Service
@DependsOn({"SFTPInitialConfigService", "SFTPInitialUserInitService"})
public class SFTPPasswordLoginService implements PasswordAuthenticator {

    private final Logger logger = LoggerFactory.getLogger(SFTPPasswordLoginService.class);

    private final BlockedHostService blockedHostService;

    private final SFTPUserService sftpUserService;

    private final PasswordEncoder passwordEncoder;

    public SFTPPasswordLoginService(PasswordEncoder passwordEncoder,
                                    SFTPUserService sftpUserService,
                                    BlockedHostService blockedHostService) {
        this.passwordEncoder = passwordEncoder;
        this.sftpUserService = sftpUserService;
        this.blockedHostService = blockedHostService;
    }

    /**
     * Authenticates the user using a password.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @param session  the server session
     * @return true if authentication succeeds, false otherwise
     * @throws PasswordChangeRequiredException if a password change is required
     * @throws AsyncAuthException if an asynchronous authentication error occurs
     */
    @Override
    public boolean authenticate(String username, String password, ServerSession session)
            throws PasswordChangeRequiredException, AsyncAuthException {

        long threadId = Thread.currentThread().threadId();
        long sessionId = session.getIoSession().getId();
        String ipAddress = ((InetSocketAddress) session.getIoSession().getRemoteAddress()).getAddress().getHostAddress();
        logger.info("Thread ID: {}, Session ID: {}, Authenticating user: {} from host: {}", threadId, sessionId,
                username, ipAddress);

        if (blockedHostService.isBlocked(ipAddress)) {
            logger.warn("Thread ID: {}, Session ID: {}, Blocked host attempted to connect: {}", threadId, sessionId,
                    ipAddress);
            return false;
        }

        Optional<SFTPUser> sftpUser = this.sftpUserService.getUserByUserName(username);
        if (sftpUser.isEmpty()) {
            logger.info("Thread ID: {}, Session ID: {}, User not found: {}", threadId, sessionId, username);
            blockedHostService.recordFailedUserAttempt(ipAddress, "User not found",
                    "SFTPPasswordLoginService", session);
            return false;
        }

        SFTPUser user = sftpUser.get();
        boolean isAuthenticated = passwordEncoder.matches(password, user.getPassword());

        if (!isAuthenticated) {
            logger.info("Thread ID: {}, Session ID: {}, Authentication failed for user: {} From Host:{}", threadId,
                    sessionId, username, ipAddress);
            blockedHostService.recordFailedPasswordAttempt(ipAddress, "Incorrect password",
                    "SFTPPasswordLoginService", session);
            return false;
        } else {
            logger.info("Thread ID: {}, Session ID: {}, Authentication succeeded for user: {} From: {}", threadId,
                    sessionId, username, ipAddress);
            if (blockedHostService.hasAttempts(ipAddress)) {
                blockedHostService.clearAttempts(ipAddress, "SFTPPasswordLoginService");
            }
        }
        return isAuthenticated;
    }
}
