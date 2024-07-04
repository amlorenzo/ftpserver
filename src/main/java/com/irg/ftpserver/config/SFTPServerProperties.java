package com.irg.ftpserver.config;

import com.irg.ftpserver.model.SFTPUser;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "sftp.server")
@Data
public class SFTPServerProperties {

    private int port;
    private String keyPath;
    private String hostKeyAlgorithm;
    private List<SFTPUser> SFTPUsers;
    private int maxWriteDataPacketLength;
    private int maxLoginAttemptThreshold;
    private long delayBetweenLoginAttempts;
}
