package com.irg.ftpserver.events;


import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.AbstractSftpEventListenerAdapter;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Qualifier("customSftpEventListener")
public class CustomSFTPEventListener extends AbstractSftpEventListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CustomSFTPEventListener.class);

    private final ConcurrentHashMap<String, AtomicLong> fileWriteSizes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> fileReadSizes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> fileAccessed = new ConcurrentHashMap<>();


    @Override
    public void open(ServerSession session, String remoteHandle, Handle localHandle) throws IOException {
        Path path = localHandle.getFile();
        logger.info(String.format("User: %s from: %s, accessed file or directory: %s",
                session.getUsername(), session.getIoSession().getRemoteAddress(), path));
    }

    @Override
    public void writing(ServerSession session, String remoteHandle, FileHandle localHandle, long offset, byte[] data
            , int dataOffset, int dataLen) throws IOException {
        Path path = localHandle.getFile();
        String filePath = getKey(session, path);
        fileWriteSizes.computeIfAbsent(filePath, k -> new AtomicLong(0)).addAndGet(dataLen);
        fileAccessed.put(filePath, true);
    }

    @Override
    public void read(ServerSession session, String remoteHandle, FileHandle localHandle, long offset, byte[] data
            , int dataOffset, int dataLen, int readLen, Throwable thrown) throws IOException {
        Path path = localHandle.getFile();
        String filePath = getKey(session, path);
        fileReadSizes.computeIfAbsent(filePath, k -> new AtomicLong(0)).addAndGet(readLen);
        fileAccessed.put(filePath, true);
    }

    @Override
    public void closed(ServerSession session, String remoteHandle, Handle localHandle, Throwable thrown) throws
            IOException {
        Path path = localHandle.getFile();
        String filePath = getKey(session, path);
        AtomicLong totalWriteSize = fileWriteSizes.remove(filePath);
        AtomicLong totalReadSize = fileReadSizes.remove(filePath);
        Boolean accessed = fileAccessed.remove(filePath);
        long fileSize = Files.size(path);

        if (totalWriteSize != null && totalWriteSize.get() > 0) {
            logger.info("User: {}, from: {}, wrote to file: {}, total data length: {}, file size: {}"
                    , session.getUsername(),session.getIoSession().getRemoteAddress(),path, totalWriteSize.get()
                    , fileSize);
        } else if (totalReadSize != null && totalReadSize.get() > 0) {
            logger.info("User: {}, from {}, read from file: {}, total data length: {}, file size: {}"
                    , session.getUsername(), session.getIoSession().getRemoteAddress(), path, totalReadSize.get()
                    , fileSize);
        } else if (accessed != null && accessed) {
            logger.info("User: {}, from {}, accessed file: {} but did not read or write any data, file size: {}"
                    , session.getIoSession().getRemoteAddress(), session.getUsername(), path, fileSize);
        }
    }

    @Override
    public void removing(ServerSession session, Path path, boolean isDirectory) throws IOException {
        long fileSize = Files.size(path);
        logger.info("User: {}, from: {}, is deleting {}: {}, file size: {}", session.getUsername()
                , session.getIoSession().getRemoteAddress(), isDirectory ? "directory" : "file", path, fileSize);

    }

    @Override
    public void removed(ServerSession session, Path path, boolean isDirectory, Throwable thrown) throws IOException {
        long fileSize = Files.size(path);
        if (thrown == null) {
            logger.info("User: {}, from: {}, successfully deleted {}: {}, file size: {}", session.getUsername()
                    , session.getIoSession().getRemoteAddress(), isDirectory ? "directory" : "file", path, fileSize);
        } else {
            logger.error("User: {}, from: {}, failed to delete {}: {} due to {}, file size: {}", session.getUsername()
                    , session.getIoSession().getRemoteAddress(), isDirectory ? "directory" : "file", path
                    , thrown.getMessage(), fileSize, thrown);
        }
    }

    @Override
    public void created(ServerSession session, Path path, Map<String, ?> attrs, Throwable thrown) throws IOException {
        if (thrown == null) {
            logger.info("User: {}, from: {}, successfully created directory: {}", session.getUsername()
                    , session.getIoSession().getRemoteAddress(), path);
        } else {
            logger.error("User: {}, from: {}, failed to create directory: {} due to {}", session.getUsername()
                    , session.getIoSession().getRemoteAddress(), path, thrown.getMessage(), thrown);
        }
    }


    private String getKey(ServerSession session, Path path) {
        return session.getUsername() + "-" + path.toString();
    }
}
