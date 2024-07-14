package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPUser;
import com.irg.ftpserver.model.PublicKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.util.ValidateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
@Data
@AllArgsConstructor
@DependsOn({"SFTPInitialUserInitService","SFTPInitialConfigService"})
public class SFTPCustomKeyPairProviderService implements KeyPairProvider {

    private static final Logger log = LoggerFactory.getLogger(SFTPCustomKeyPairProviderService.class);

    private final SFTPUserService sftpUserService;

    private final List<KeyPair> keyPairs;



    @Override
    public Iterable<String> getKeyTypes(SessionContext session) throws IOException, GeneralSecurityException {
        return StreamSupport.stream(loadKeys(session).spliterator(), false)
                .map(this::extractKeyType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session) throws IOException, GeneralSecurityException {
        String username = session.getUsername();
        ValidateUtils.checkNotNullAndNotEmpty(username, "Username is null or empty");

        Optional<SFTPUser> sftpUser = sftpUserService.getUserByUserName(username);
        if (sftpUser.isEmpty()) {
            throw new GeneralSecurityException("User not found: " + username);
        }

        List<String> publicKeys = sftpUser.get().getPublicKeys().stream()
                .map(PublicKey::getPublicKey)
                .toList();

        return publicKeys.stream()
                .map(this::createKeyPair)
                .collect(Collectors.toList());
    }

    @Override
    public KeyPair loadKey(SessionContext session, String type) throws IOException, GeneralSecurityException {
        ValidateUtils.checkNotNullAndNotEmpty(type, "No key type to load");
        return StreamSupport.stream(loadKeys(session).spliterator(), false)
                .filter(kp -> type.equals(extractKeyType(kp)))
                .findFirst()
                .orElseThrow(() -> new GeneralSecurityException("No key of type: " + type));
    }

    private KeyPair createKeyPair(String publicKeyStr) {
        try {
            String[] parts= publicKeyStr.split(" ");
            byte[] keyBytes = Base64.getDecoder().decode(parts[1]);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return new KeyPair(keyFactory.generatePublic(spec), null);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to create key pair: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create key pair" + e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            log.error("Base64 decoding failed: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid Key Spec: " + e.getMessage(), e);
        }
    }

    private String extractKeyType(KeyPair keyPair) {
        return keyPair.getPublic().getAlgorithm();
    }
}
