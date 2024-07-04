package com.irg.ftpserver.config;

import com.irg.ftpserver.events.CustomSFTPEventListener;
import com.irg.ftpserver.events.CustomSFTPSessionListener;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@SpringBootTest
@ContextConfiguration(classes = {TestConfig.class, SFTPServerConfig.class})
public class SFTPServerConfigTest {

    @Autowired
    private SFTPServerConfig sftpServerConfig;

    @MockBean
    private CustomSFTPEventListener customSftpEventListener;

    @MockBean
    private CustomSFTPSessionListener customSFTPSessionListener;

    @MockBean
    private KeyPairProvider keyPairProvider;

    @Autowired
    private SFTPServerProperties sftpServerProperties;

    @BeforeEach
    public void setUp() {
        // Mock the behavior of KeyPairProvider to return a set of key types
        try {
            Mockito.when(keyPairProvider.getKeyTypes(Mockito.any())).thenReturn(Collections.singleton("RSA"));
        } catch (IOException | GeneralSecurityException e) {
            Assertions.fail("Exception in setting up mocks: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test SshServer bean creation")
    public void testSshServerBean() {
        SshServer sshServer = sftpServerConfig.sshServer(customSftpEventListener, customSFTPSessionListener);
        Assertions.assertNotNull(sshServer);
        Assertions.assertEquals(sftpServerProperties.getPort(), sshServer.getPort());
    }

    @Test
    @DisplayName("Test createKeyPairProvider method")
    public void testCreateKeyPairProvider() {
        KeyPairProvider keyPairProvider = sftpServerConfig.createKeyPairProvider();
        Assertions.assertNotNull(keyPairProvider);
        try {
            Assertions.assertEquals("RSA", keyPairProvider.getKeyTypes(Mockito.any()).iterator().next());
        } catch (IOException | GeneralSecurityException e) {
            Assertions.fail("Exception in testCreateKeyPairProvider: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test createPasswordAuthenticator method")
    public void testCreatePasswordAuthenticator() {
        PasswordAuthenticator passwordAuthenticator = sftpServerConfig.createPasswordAuthenticator();
        Assertions.assertNotNull(passwordAuthenticator);
        // Check if the user map is correctly populated
        Assertions.assertTrue(
                sftpServerProperties.getSFTPUsers().stream()
                        .anyMatch(user -> "testUser".equals(user.getUsername()) && "password".equals(user.getPassword()))
        );
        boolean isAuthenticated = passwordAuthenticator.authenticate("testUser", "password", null);
        System.out.println("Authentication result: " + isAuthenticated);
        Assertions.assertTrue(isAuthenticated);
        Assertions.assertFalse(passwordAuthenticator.authenticate("testUser", "wrongPassword", null));
    }

    @Test
    @DisplayName("Test createFileSystemFactory method")
    public void testCreateFileSystemFactory() {
        Assertions.assertNotNull(sftpServerConfig.createFileSystemFactory());
    }
}
