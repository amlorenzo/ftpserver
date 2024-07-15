package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPPublicKey;
import com.irg.ftpserver.model.SFTPUser;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
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
public class SFTPPublicKeyLoginService implements PublickeyAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(SFTPPublicKeyLoginService.class);

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
        String ipAddress = ((InetSocketAddress) session.getIoSession().getRemoteAddress()).getAddress().getHostAddress();
        if (blockedHostService.isBlocked(ipAddress)) {
            logger.warn("Blocked host attempted to connect: {}", ipAddress);
            return false;
        }

        Optional<SFTPUser> sftpUser = this.sftpUserService.getUserByUserName(username);
        if (sftpUser.isEmpty()) {
            logger.info("User not found: {}", username);
            blockedHostService.recordFailedUserAttempt(ipAddress, "User not found",
                    "SFTPPublicKeyLoginService", session);
            return false;
        }

        List<SFTPPublicKey> sftpPublicKeys = sftpUser.get().getSFTPPublicKeys().stream()
                .filter(SFTPPublicKey::isEnabled)
                .toList();

        for (SFTPPublicKey sftpPublicKey : sftpPublicKeys) {
            try {
                PublicKey publicKey = createPublicKey(sftpPublicKey.getPublicKey());
                if (publicKey.equals(key)) {
                    logger.info("Public key authentication succeeded for user: {}, with IP address {}", username,
                            session.getIoSession().getRemoteAddress());
                    blockedHostService.clearAttempts(ipAddress, "SFTPPublicKeyLoginService");
                    return true;
                }
            } catch (GeneralSecurityException e) {
                logger.error("Error loading keys for user {}: {}", username, e.getMessage(), e);
            }
        }

        blockedHostService.recordFailedUserAttempt(ipAddress, "Public key mismatch", "SFTPPublicKeyLoginService", session);
        return false;
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
