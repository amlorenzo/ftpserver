package com.irg.ftpserver.config;

import com.irg.ftpserver.model.SFTPUser;
import com.irg.ftpserver.model.User;
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
    private List<User> users;
    //SFTP Login properties
    private int maxWriteDataPacketLength;
    private int maxLoginAttemptThreshold;
    private int delayBetweenLoginAttempts;
    //Executor properties
    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int queueCapacity;
    //Default value is true
    private boolean initialPasswordChangeRequired = true;
}
