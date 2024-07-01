package com.irg.ftpserver.config;

import com.irg.ftpserver.model.User;
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
        User testUser = new User("testUser", "password", "/home/testUser");
        properties.setUsers(List.of(testUser));
        return properties;
    }
}
