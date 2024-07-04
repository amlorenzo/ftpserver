package com.irg.ftpserver.config;

import com.irg.ftpserver.model.SFTPUser;
import org.apache.sshd.server.SshServer;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class TestConfig {

    @Bean(name = "mockSshServer")
    public SshServer mockSshServer() {
        return Mockito.mock(SshServer.class);
    }
    @Primary
    @Bean
    public SFTPServerProperties testSFTPServerProperties() {
        SFTPServerProperties properties = new SFTPServerProperties();
        properties.setPort(2221);
        properties.setKeyPath("/path/to/key");
        properties.setHostKeyAlgorithm("RSA");
        properties.setMaxWriteDataPacketLength(32768);
        SFTPUser testSFTPUser = new SFTPUser("testSFTPUser", "password", "/home/testSFTPUser");
        properties.setSFTPUsers(List.of(testSFTPUser));
        return properties;
    }
}
