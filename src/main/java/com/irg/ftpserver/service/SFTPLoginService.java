package com.irg.ftpserver.service;

import com.irg.ftpserver.events.HostBlockedEvent;
import com.irg.ftpserver.model.SFTPUser;
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
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import com.irg.ftpserver.model.SFTPPublicKey;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@DependsOn({"SFTPInitialConfigService", "SFTPInitialUserInitService"})
public class SFTPLoginService implements PasswordAuthenticator, ApplicationEventPublisherAware, PublickeyAuthenticator {

    private final Logger logger = LoggerFactory.getLogger(SFTPLoginService.class);

    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();

    private final int maxLoginAttempts;

    private final int delayBetweenAttempts;

    private ApplicationEventPublisher eventPublisher;

    private final SFTPUserService sftpUserService;

    private final SFTPConfigurationService sftpConfigurationService;

    private final BlockedHostService blockedHostService;

    private final PasswordEncoder passwordEncoder;


    public SFTPLoginService(PasswordEncoder passwordEncoder,
                            SFTPUserService sftpUserService,
                            SFTPConfigurationService sftpConfigurationService,
                            BlockedHostService blockedHostService) {

        this.passwordEncoder = passwordEncoder;
        this.sftpUserService = sftpUserService;
        this.sftpConfigurationService = sftpConfigurationService;
        this.blockedHostService = blockedHostService;

        this.maxLoginAttempts = this.sftpConfigurationService.getLatestConfiguration().getMaxLoginAttemptThreshold();
        this.delayBetweenAttempts = this.sftpConfigurationService.getLatestConfiguration()
                .getDelayBetweenLoginAttempts();

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
            blockedHostService.recordFailedAttempt(ipAddress,reason);
            logger.warn("Blocking Host: {} after {} failed login attempts", ipAddress, attemptCount);
            loginAttempts.remove(ipAddress);
            eventPublisher.publishEvent(new HostBlockedEvent(session, ipAddress));
        }
    }

    public boolean isBlocked(String ipAddress){

        return blockedHostService.isBlocked(ipAddress);
    }

    public void clearAttempts(String ipAddress){

        loginAttempts.remove(ipAddress);
        logger.info("Cleared attempts for host: {}", ipAddress);
    }

    public void unblock(String ipAddress){

        blockedHostService.unblock(ipAddress);
        logger.info("Unblocked host: {}", ipAddress);
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

        Optional<SFTPUser> sftpUser = this.sftpUserService.getUserByUserName(username);
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

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException {

        Optional <SFTPUser> sftpUser = this.sftpUserService.getUserByUserName(username);

        if (sftpUser.isEmpty()) {
            logger.info("User not found: {}", username);
            return false;
        }

        List<SFTPPublicKey> SFTPPublicKeys = sftpUser.get().getSFTPPublicKeys().stream()
                .filter(SFTPPublicKey::isEnabled)
                .toList();

        for (SFTPPublicKey SFTPPublicKey : SFTPPublicKeys) {
            try {
                PublicKey publicKey = createPublicKey(SFTPPublicKey.getPublicKey());
                if (publicKey.equals(key)) {
                    logger.info("Public key authentication succeeded for user: {}, with IPaddress{}", username,
                            session.getIoSession().getRemoteAddress());
                    return true;
                }
            } catch (GeneralSecurityException e) {
                logger.error("Error loading keys for user {}: {}", username, e.getMessage(), e);            }
        }

        return false;
    }

    private PublicKey createPublicKey(String publicKeyString) throws GeneralSecurityException {
        try {
            String[] parts = publicKeyString.split(" ");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid public key format");
            }
            //Situation with white space causing problems
            String base64Part = parts[1].replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(base64Part);
            RSAPublicKeySpec spec = decodeRSAPublicKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(sftpConfigurationService.getLatestConfiguration()
                    .getHostKeyAlgorithm());
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            logger.error("Error creating public key from string: {}", e.getMessage(), e);
            throw new GeneralSecurityException("Failed to create public key: " + e.getMessage(), e);
        }
    }

    private RSAPublicKeySpec decodeRSAPublicKeySpec(byte[] keyBytes) throws GeneralSecurityException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(keyBytes);
             DataInputStream dis = new DataInputStream(bais)) {

            // Decode the "ssh-rsa" prefix
            int len = dis.readInt();
            byte[] type = new byte[len];
            dis.readFully(type);
            if (!"ssh-rsa".equals(new String(type))) {
                logger.error("Invalid key type: {}", new String(type));
                throw new GeneralSecurityException("Invalid keytype: " + new String(type));
            }

            // Decode the exponent
            len = dis.readInt();
            byte[] exponentBytes = new byte[len];
            dis.readFully(exponentBytes);
            BigInteger exponent = new BigInteger(exponentBytes);

            // Decode the modulus
            len = dis.readInt();
            byte[] modulusBytes = new byte[len];
            dis.readFully(modulusBytes);
            BigInteger modulus = new BigInteger(modulusBytes);

            // Return the RSAPublicKeySpec
            return new RSAPublicKeySpec(modulus, exponent);
        } catch (IOException e) {
            logger.error("Error decoding RSA public key: {}", e.getMessage(), e);
            throw new GeneralSecurityException("Failed to decode RSA public key", e);
        }
    }
}

