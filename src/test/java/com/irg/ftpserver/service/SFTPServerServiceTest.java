package com.irg.ftpserver.service;

import com.irg.ftpserver.config.TestConfig;
import org.apache.sshd.server.SshServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;




@SpringBootTest
@Import(TestConfig.class)
public class SFTPServerServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(SFTPServerServiceTest.class);

    @Autowired
    @Qualifier("mockSshServer")
    private SshServer sshServer;

    private SFTPServerService sftpServerService;

    @BeforeEach
    public void setUp() {
        // Reset the mock to avoid cross-test contamination
        Mockito.reset(sshServer);
        sftpServerService = new SFTPServerService(sshServer);
    }

    @Test
    @DisplayName("Test SFTP server starts successfully")
    public void testStart() throws IOException {
        Mockito.doNothing().when(sshServer).start();
        sftpServerService.start();
        Mockito.verify(sshServer, Mockito.times(1)).start();
        logger.info("SFTP Server started successfully");
    }

    @Test
    @DisplayName("Test SFTP server start with error handling")
    public void testStartWithError() throws IOException {
        Mockito.doThrow(new IOException("Start Error")).when(sshServer).start();
        sftpServerService.start();
        Mockito.verify(sshServer, Mockito.times(1)).start();
        logger.info("Handled start error successfully");
    }

    @Test
    @DisplayName("Test SFTP server stops successfully")
    public void testStop() throws IOException {
        Mockito.doNothing().when(sshServer).stop();
        sftpServerService.stop();
        Mockito.verify(sshServer, Mockito.times(1)).stop();
        logger.info("SFTP Server stopped successfully");
    }

    @Test
    @DisplayName("Test SFTP server stop with error handling")
    public void testStopWithError() throws IOException {
        Mockito.doThrow(new IOException("Stop Error")).when(sshServer).stop();
        sftpServerService.stop();
        Mockito.verify(sshServer, Mockito.times(1)).stop();
        logger.info("Handled stop error successfully");
    }

    @Test
    @DisplayName("Test SFTP server lifecycle")
    public void testLifecycle() throws IOException {
        Mockito.doNothing().when(sshServer).start();
        Mockito.doNothing().when(sshServer).stop();
        sftpServerService.start();
        Mockito.verify(sshServer, Mockito.times(1)).start();
        sftpServerService.stop();
        Mockito.verify(sshServer, Mockito.times(1)).stop();
        logger.info("SFTP Server lifecycle test completed successfully");
    }
}
