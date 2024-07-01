package com.irg.ftpserver.config;

import com.irg.ftpserver.events.CustomSFTPEventListener;
import com.irg.ftpserver.events.CustomSFTPSessionListener;
import com.irg.ftpserver.model.User;
import lombok.RequiredArgsConstructor;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.SftpModuleProperties;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.nio.file.Paths;
import java.util.*;


@Configuration
@RequiredArgsConstructor
public class SFTPServerConfig {
    private static final Logger logger = LoggerFactory.getLogger(SFTPServerConfig.class);
    private final SFTPServerProperties sftpServerProperties;

    @Bean
    public SshServer sshServer(CustomSFTPEventListener customSftpEventListener
            , CustomSFTPSessionListener customSFTPSessionListener) {
        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(sftpServerProperties.getPort());
        sshServer.setKeyPairProvider(createKeyPairProvider());
        sshServer.setPasswordAuthenticator(createPasswordAuthenticator());
        sshServer.setFileSystemFactory(createFileSystemFactory());

        // Logging maximum write data packet length property
        int maxWriteDataPacketLength = sftpServerProperties.getMaxWriteDataPacketLength();
        PropertyResolverUtils.updateProperty(
                sshServer,
                SftpModuleProperties.MAX_WRITEDATA_PACKET_LENGTH.getName(),
                maxWriteDataPacketLength
        );
        logger.info("Setting max write data packet length to: {}", maxWriteDataPacketLength);


        // Register Event Listener
        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory();
        sftpSubsystemFactory.addSftpEventListener(customSftpEventListener);
        sshServer.addSessionListener(customSFTPSessionListener);
        sshServer.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));

        logger.info("SFTP server started on port: {}", sshServer.getPort());
        return sshServer;
    }

    @Bean
    public KeyPairProvider createKeyPairProvider() {
        AbstractGeneratorHostKeyProvider hostKeyProvider =
                new SimpleGeneratorHostKeyProvider(Paths.get(sftpServerProperties.getKeyPath()));
        hostKeyProvider.setAlgorithm(sftpServerProperties.getHostKeyAlgorithm());
        return hostKeyProvider;
    }

    @Bean
    public PasswordAuthenticator createPasswordAuthenticator() {
        Map<String, String> userCredentials = new HashMap<>();
        for (User user : sftpServerProperties.getUsers()) {
            userCredentials.put(user.getUsername(), user.getPassword());
        }

        return new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                return userCredentials.containsKey(username) && userCredentials.get(username).equals(password);
            }
        };
    }

    @Bean
    public VirtualFileSystemFactory createFileSystemFactory() {
        VirtualFileSystemFactory factory = new VirtualFileSystemFactory();
        for (User user : sftpServerProperties.getUsers()) {
            factory.setUserHomeDir(user.getUsername(), Paths.get(user.getDirectory()));
        }

        return factory;
    }
}