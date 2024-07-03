package com.irg.ftpserver.service;

import com.irg.ftpserver.config.SFTPServerProperties;
import com.irg.ftpserver.model.User;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class SFTPFileSystemService {
    private final SFTPServerProperties sftpServerProperties;

    /***
     * This class needs to eventually modified to return a VirtualFileSystemFactory dynamically
     * will need to use the SpringCloudPackage for this.
     * @return VirtualFileSystemFactory
     */
    public VirtualFileSystemFactory createVirtualFileSystemFactory() {
        VirtualFileSystemFactory virtualFileSystemFactory = new VirtualFileSystemFactory();
        for(User user : sftpServerProperties.getUsers()) {
            virtualFileSystemFactory.setUserHomeDir(user.getUsername(), Paths.get(user.getDirectory()));
        }
        return virtualFileSystemFactory;
    }

    public SFTPFileSystemService(SFTPServerProperties sftpServerProperties) {
        this.sftpServerProperties = sftpServerProperties;
    }
}
