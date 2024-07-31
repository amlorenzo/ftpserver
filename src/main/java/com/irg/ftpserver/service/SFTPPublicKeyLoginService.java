package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPPublicKey;
import com.irg.ftpserver.model.SFTPUser;
import org.apache.sshd.common.AttributeRepository.AttributeKey;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing public key authentication for SFTP.
 */
@Service
@DependsOn({"SFTPInitialConfigService", "SFTPInitialUserInitService"})
@Scope("singleton")
public class SFTPPublicKeyLoginService implements PublickeyAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(SFTPPublicKeyLoginService.class);
    private static final AttributeKey<Boolean> AUTHENTICATED_ATTRIBUTE_KEY = new AttributeKey<>();
    private final SFTPUserService sftpUserService;
    private final SFTPConfigurationService sftpConfigurationService;
    private final BlockedHostService blockedHostService;

    public SFTPPublicKeyLoginService(SFTPUserService sftpUserService,
                                     SFTPConfigurationService sftpConfigurationService,
                                     BlockedHostService blockedHostService) {
        this.sftpUserService = sftpUserService;
        this.sftpConfigurationService = sftpConfigurationService;
        this.blockedHostService = blockedHostService;
    }

    /**
     * Authenticates the user using a public key.
     *
     * @param username the username of the user
     * @param key      the public key of the user
     * @param session  the server session
     * @return true if authentication succeeds, false otherwise
     * @throws AsyncAuthException if an asynchronous authentication error occurs
     */
    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) throws AsyncAuthException {
        long threadId = Thread.currentThread().threadId();
        long sessionId = session.getIoSession().getId();
        String ipAddress = ((InetSocketAddress) session.getIoSession().getRemoteAddress()).getAddress().getHostAddress();

        // Synchronize on the session to prevent multiple threads from authenticating the same session
        synchronized (session) {
            Boolean isAuthenticated = session.getAttribute(AUTHENTICATED_ATTRIBUTE_KEY);
            if (isAuthenticated != null && isAuthenticated) {
                logger.info("Thread ID: {}, Session ID: {}, Session already authenticated for user: {}",
                        threadId, sessionId, username);
                return true;
            }

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
                        "SFTPPublicKeyLoginService", session);
                return false;
            }

            List<SFTPPublicKey> sftpPublicKeys = sftpUser.get().getSFTPPublicKeys().stream()
                    .filter(SFTPPublicKey::isEnabled)
                    .toList();

            boolean matchFound = sftpPublicKeys.stream().anyMatch(sftpPublicKey -> {
                try {
                    PublicKey publicKey = createPublicKey(sftpPublicKey.getPublicKey());
                    if (publicKey.equals(key)) {
                        logger.info("Thread ID: {}, Session ID: {}, Public key matched for user: {}",
                                threadId, sessionId, username);
                        session.setAttribute(AUTHENTICATED_ATTRIBUTE_KEY, true);
                        return true;
                    }
                } catch (GeneralSecurityException e) {
                    logger.error("Thread ID: {}, Session ID: {}, Error loading keys for user {}: {}",
                            threadId, sessionId, username, e.getMessage(), e);
                }
                return false;
            });

            if (matchFound) {
                return true;
            } else {
                blockedHostService.recordFailedPasswordAttempt(ipAddress, "Public key mismatch",
                        "SFTPPublicKeyLoginService", session);
                return false;
            }
        }
    }


    /**
     * Creates a public key from the given public key string.
     *
     * @param publicKeyString the public key string
     * @return the public key
     * @throws GeneralSecurityException if a security error occurs
     */
    private PublicKey createPublicKey(String publicKeyString) throws GeneralSecurityException {
        try {
            String[] parts = publicKeyString.split(" ");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid public key format");
            }
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

    /**
     * Decodes the RSA public key specification from the given key bytes.
     *
     * @param keyBytes the key bytes
     * @return the RSA public key specification
     * @throws GeneralSecurityException if a security error occurs
     */
    private RSAPublicKeySpec decodeRSAPublicKeySpec(byte[] keyBytes) throws GeneralSecurityException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(keyBytes);
             DataInputStream dis = new DataInputStream(bais)) {

            int len = dis.readInt();
            byte[] type = new byte[len];
            dis.readFully(type);
            if (!"ssh-rsa".equals(new String(type))) {
                logger.error("Invalid key type: {}", new String(type));
                throw new GeneralSecurityException("Invalid key type: " + new String(type));
            }

            len = dis.readInt();
            byte[] exponentBytes = new byte[len];
            dis.readFully(exponentBytes);
            BigInteger exponent = new BigInteger(exponentBytes);

            len = dis.readInt();
            byte[] modulusBytes = new byte[len];
            dis.readFully(modulusBytes);
            BigInteger modulus = new BigInteger(modulusBytes);

            return new RSAPublicKeySpec(modulus, exponent);
        } catch (IOException e) {
            logger.error("Error decoding RSA public key: {}", e.getMessage(), e);
            throw new GeneralSecurityException("Failed to decode RSA public key", e);
        }
    }
}
