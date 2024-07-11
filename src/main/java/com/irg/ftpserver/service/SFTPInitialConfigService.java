package com.irg.ftpserver.service;

import com.irg.ftpserver.config.SFTPServerProperties;
import com.irg.ftpserver.model.SFTPServerConfiguration;
import com.irg.ftpserver.repository.SFTPServerConfigurationRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@Data
@AllArgsConstructor
public class SFTPInitialConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SFTPInitialConfigService.class);

    private SFTPServerProperties sftpServerProperties;

    private SFTPServerConfigurationRepository sftpServerConfigurationRepository;

    @PostConstruct
    public void initializeServerConfig() {
        if (sftpServerConfigurationRepository.findAll().isEmpty()) {
            SFTPServerConfiguration sftpServerConfiguration = new SFTPServerConfiguration();
            sftpServerConfiguration.setPort(sftpServerProperties.getPort());
            sftpServerConfiguration.setKeyPath(sftpServerProperties.getKeyPath());
            sftpServerConfiguration.setHostKeyAlgorithm(sftpServerProperties.getHostKeyAlgorithm());
            sftpServerConfiguration.setMaxWriteDataPacketLength(sftpServerProperties.getMaxWriteDataPacketLength());
            sftpServerConfiguration.setMaxLoginAttemptThreshold(sftpServerProperties.getMaxLoginAttemptThreshold());
            sftpServerConfiguration.setDelayBetweenLoginAttempts(sftpServerProperties.getDelayBetweenLoginAttempts());
            sftpServerConfiguration.setCorePoolSize(sftpServerProperties.getCorePoolSize());
            sftpServerConfiguration.setMaxPoolSize(sftpServerProperties.getMaxPoolSize());
            sftpServerConfiguration.setKeepAliveTime(sftpServerProperties.getKeepAliveTime());
            sftpServerConfiguration.setQueueCapacity(sftpServerProperties.getQueueCapacity());
            sftpServerConfigurationRepository.save(sftpServerConfiguration);
        }

    }
}