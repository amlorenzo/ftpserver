package com.irg.ftpserver.config;

import com.irg.ftpserver.events.CustomSFTPEventListener;
import com.irg.ftpserver.events.CustomSFTPSessionListener;
import com.irg.ftpserver.service.*;
import com.irg.ftpserver.service.SFTPPasswordLoginService;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.threads.ThreadUtils;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.SftpModuleProperties;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import java.nio.file.Paths;
import java.util.*;

@Configuration
@DependsOn({"SFTPInitialConfigService", "SFTPInitialUserInitService"})
public class SFTPServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SFTPServerConfig.class);

    private SFTPCustomCloseableExecutorService customExecutorService;

    private final SFTPConfigurationService sftpConfigurationService;

    public SFTPServerConfig(SFTPConfigurationService sftpConfigurationService) {

        this.sftpConfigurationService = sftpConfigurationService;
    }

    @Bean
    public SshServer sshServer(CustomSFTPEventListener customSftpEventListener
            , CustomSFTPSessionListener customSFTPSessionListener,
                               SFTPPasswordLoginService sftpPasswordLoginService,
                               SFTPFileSystemService sftpFileSystemService,
                               SFTPPublicKeyLoginService sftpPublicKeyLoginService){

        // Retrieving variables with latest configuration for code clarity
        int corePoolSize = this.sftpConfigurationService.getLatestConfiguration().getCorePoolSize();
        int maxPoolSize = this.sftpConfigurationService.getLatestConfiguration().getMaxPoolSize();
        int keepAliveTime = this.sftpConfigurationService.getLatestConfiguration().getKeepAliveTime();
        int queueCapacity = this.sftpConfigurationService.getLatestConfiguration().getQueueCapacity();
        int port = this.sftpConfigurationService.getLatestConfiguration().getPort();
        long maxIdleTime = this.sftpConfigurationService.getLatestConfiguration().getMaxIdleTime();
        int maxWriteDataPacketLength = this.sftpConfigurationService.getLatestConfiguration()
                .getMaxWriteDataPacketLength();

        SFTPExecutorService sftpExecutorService = new SFTPExecutorService(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                queueCapacity
        );
        logger.info("Creating custom executor service with core pool size: {}, max pool size: {}, keep alive time: " +
                        "{}, queue capacity: {}",
                corePoolSize, maxPoolSize, keepAliveTime, queueCapacity);




        // Create custom executor service
         this.customExecutorService =
                new SFTPCustomCloseableExecutorService(sftpExecutorService);

        SftpSubsystemFactory sftpSubsystemFactory = new SftpSubsystemFactory.Builder()
                .withExecutorServiceProvider(() -> ThreadUtils.noClose(customExecutorService))
                .build();

        //Set SSH Server Properties
        SshServer sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        sshServer.setPasswordAuthenticator(sftpPasswordLoginService);
        sshServer.setPublickeyAuthenticator(sftpPublicKeyLoginService);
        sshServer.setKeyPairProvider(createKeyPairProvider());
        sshServer.setFileSystemFactory(sftpFileSystemService);

        // Set idle timeout property
        sshServer.getProperties().put(CoreModuleProperties.IDLE_TIMEOUT.getName(), maxIdleTime);
        logger.info("Setting session timeout to: {} ms", maxIdleTime);



        // Logging maximum write data packet length property
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

        String keyPath = this.sftpConfigurationService.getLatestConfiguration().getKeyPath();
        String hostKeyAlgorithm = this.sftpConfigurationService.getLatestConfiguration().getHostKeyAlgorithm();

        AbstractGeneratorHostKeyProvider hostKeyProvider =
                new SimpleGeneratorHostKeyProvider(Paths.get(keyPath));
        hostKeyProvider.setAlgorithm(hostKeyAlgorithm);
        logger.info("Host key algorithm set to: {}", hostKeyAlgorithm);
        logger.info("Host key path set to: {}", hostKeyProvider.getPath());
        return hostKeyProvider;
    }
}