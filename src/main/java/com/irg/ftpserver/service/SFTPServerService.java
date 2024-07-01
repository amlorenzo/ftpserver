package com.irg.ftpserver.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.sshd.server.SshServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
/***
 * SFTPServerService class is responsible for starting the SFTP server.
 * Configuration of the server is done in the SFTPServerConfig class.
 * ***/
public class SFTPServerService {

    private static final Logger logger = LoggerFactory.getLogger(SFTPServerService.class);
    private final SshServer sshServer;

    public SFTPServerService(SshServer sshServer) {
        this.sshServer = sshServer;
    }

    @PostConstruct
    public void start() {
        try {
            sshServer.start();
            logger.info("SFTP Server started successfully on port." + sshServer.getPort());
        } catch (IOException e) {
            logger.error("Error starting SFTP Server: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stop() {
        try {
            sshServer.stop();
            logger.info("SFTP Server stopped successfully.");
        } catch (IOException e) {
            logger.error("Error stopping SFTP Server: {}", e.getMessage());
        }
    }




}


