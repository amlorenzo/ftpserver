package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPServerConfiguration;
import com.irg.ftpserver.repository.SFTPServerConfigurationRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
@AllArgsConstructor
@DependsOn({"SFTPInitialConfigService", "SFTPInitialUserInitService"})
public class SFTPConfigurationService{

    private static final Logger logger = LoggerFactory.getLogger(SFTPConfigurationService.class);

    private final SFTPServerConfigurationRepository sftpServerConfigurationRepository;

    public SFTPServerConfiguration getLatestConfiguration() {
        List<SFTPServerConfiguration> sftpServerConfigurations = sftpServerConfigurationRepository.findAll();
        if (sftpServerConfigurations.isEmpty()) {
            logger.info("No configuration found in the database");
            throw new RuntimeException("SFTP Server configuration not found");
        }
        return sftpServerConfigurations.getLast();
    }
}
