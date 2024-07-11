package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPUser;
import com.irg.ftpserver.repository.SFTPUserRepository;
import lombok.AllArgsConstructor;
import org.apache.sshd.common.file.root.RootedFileSystemProvider;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class SFTPFileSystemService extends VirtualFileSystemFactory{
    private static final Logger logger = LoggerFactory.getLogger(SFTPFileSystemService.class);

    private final SFTPUserRepository sftpUserRepository;

    private final Map<String, Path> homeDirs = new ConcurrentHashMap<>();

    @Override
    public FileSystem createFileSystem(SessionContext session) throws IOException {

        Path dir = getUserHomeDir(session);

        if (dir == null) {
            throw new InvalidPathException(session.getUsername(), "Cannot resolve home directory");
        }

        return new RootedFileSystemProvider().newFileSystem(dir, Collections.emptyMap());
    }
    @Override
    public Path getUserHomeDir(SessionContext session) throws IOException{

        String username = session.getUsername();
        Path homeDir = homeDirs.get(username);

        if (homeDir == null) {
            SFTPUser user = sftpUserRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            homeDir = Paths.get(user.getDirectory());
            if (!Files.exists(homeDir)) {
                Files.createDirectories(homeDir);
                logger.info("Home directory created: {} For user: {}", homeDir, username);
            }
            homeDirs.put(username, homeDir);
        }
        return homeDir;
    }

}
