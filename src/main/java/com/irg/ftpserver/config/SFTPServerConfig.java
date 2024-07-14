package com.irg.ftpserver.config;

import com.irg.ftpserver.events.CustomSFTPEventListener;
import com.irg.ftpserver.events.CustomSFTPSessionListener;
import com.irg.ftpserver.repository.SFTPServerConfigurationRepository;
import com.irg.ftpserver.service.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.threads.ThreadUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.SftpModuleProperties;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

@Configuration
@DependsOn({"SFTPInitialConfigService", "SFTPInitialUserInitService"})
public class SFTPServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SFTPServerConfig.class);

    private SFTPCustomCloseableExecutorService customExecutorService;

    private final SFTPCustomKeyPairProviderService customKeyPairProviderService;

    private final SFTPConfigurationService sftpConfigurationService;

    public SFTPServerConfig(SFTPCustomKeyPairProviderService customKeyPairProviderService,
                            SFTPConfigurationService sftpConfigurationService) {

        this.customKeyPairProviderService = customKeyPairProviderService;
        this.sftpConfigurationService = sftpConfigurationService;
    }


    @Bean
    public SshServer sshServer(CustomSFTPEventListener customSftpEventListener
            , CustomSFTPSessionListener customSFTPSessionListener,
                               SFTPLoginService sftpLoginService,
                               SFTPFileSystemService sftpFileSystemService) {

        SFTPExecutorService sftpExecutorService = new SFTPExecutorService(
                this.sftpConfigurationService.getLatestConfiguration().getCorePoolSize(),
                this.sftpConfigurationService.getLatestConfiguration().getMaxPoolSize(),
                this.sftpConfigurationService.getLatestConfiguration().getKeepAliveTime(),
                this.sftpConfigurationService.getLatestConfiguration().getQueueCapacity()
        );

        // Create custom executor service
         this.customExecutorService =
                new SFTPCustomCloseableExecutorService(sftpExecutorService);

        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory.Builder()
                .withExecutorServiceProvider(() -> ThreadUtils.noClose(customExecutorService))
                .build();

        //Set SSH Server Properties
        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(sftpConfigurationService.getLatestConfiguration().getPort());
        sshServer.setPasswordAuthenticator(sftpLoginService);
        sshServer.setKeyPairProvider(createKeyPairProvider());
        sshServer.setFileSystemFactory(sftpFileSystemService);

        // Logging maximum write data packet length property
        int maxWriteDataPacketLength = sftpConfigurationService.getLatestConfiguration().getMaxWriteDataPacketLength();
        PropertyResolverUtils.updateProperty(
                sshServer,
                SftpModuleProperties.MAX_WRITEDATA_PACKET_LENGTH.getName(),
                maxWriteDataPacketLength
        );
        logger.info("Setting max write data packet length to: {}", maxWriteDataPacketLength);


        // Register Event Listener
        sftpSubsystemFactory.addSftpEventListener(customSftpEventListener);
        sshServer.addSessionListener(customSFTPSessionListener);
        sshServer.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory));

        return sshServer;
    }

    @Bean
    public KeyPairProvider createKeyPairProvider() {
        AbstractGeneratorHostKeyProvider hostKeyProvider =
                new SimpleGeneratorHostKeyProvider(Paths.get(sftpConfigurationService.getLatestConfiguration().getKeyPath()));
        hostKeyProvider.setAlgorithm(sftpConfigurationService.getLatestConfiguration().getHostKeyAlgorithm());
        return hostKeyProvider;
    }
}