package com.irg.ftpserver.events;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.Handle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CustomSFTPEventListenerTest {


    private CustomSFTPEventListener customSFTPEventListener;
    private ServerSession serverSession;
    private FileHandle fileHandle;
    private Handle handle;
    private ListAppender<ILoggingEvent> listAppender;

    @Mock
    private Path path;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        customSFTPEventListener = new CustomSFTPEventListener();
        serverSession = Mockito.mock(ServerSession.class);
        fileHandle = Mockito.mock(FileHandle.class);
        handle = Mockito.mock(Handle.class);

        // Mock session details
        when(serverSession.getUsername()).thenReturn("testUser");
        when(serverSession.getIoSession()).thenReturn(Mockito.mock(org.apache.sshd.common.io.IoSession.class));
        when(serverSession.getIoSession().getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1"
                , 2221));

        // Set up Logback ListAppender
        Logger logger = (Logger) LoggerFactory.getLogger(CustomSFTPEventListener.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    @DisplayName("Test writing method log")
    public void testWriting() throws IOException {
        when(fileHandle.getFile()).thenReturn(path);

        customSFTPEventListener.writing(serverSession, "remoteHandle", fileHandle, 0, new byte[0], 0, 100);

        // Verify behavior
        verify(fileHandle, times(1)).getFile();
    }

    @Test
    @DisplayName("Test read method log")
    public void testRead() throws IOException {
        when(fileHandle.getFile()).thenReturn(path);

        customSFTPEventListener.read(serverSession, "remoteHandle", fileHandle, 0, new byte[0], 0, 100, 100, null);

        // Verify behavior
        verify(fileHandle, times(1)).getFile();
    }

    @Test
    @DisplayName("Test open method log")
    public void testOpen() throws IOException {
        when(handle.getFile()).thenReturn(path);

        customSFTPEventListener.open(serverSession, "remoteHandle", handle);

        // Verify log output
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).isNotEmpty();
        assertThat(logsList.get(0).getFormattedMessage()).contains("accessed");
    }

    @Test
    @DisplayName("Test closed method log")
    public void testClosed() throws IOException {
        // Prepare mock conditions
        when(handle.getFile()).thenReturn(path);
        AtomicLong mockWriteSize = new AtomicLong(1024L);
        AtomicLong mockReadSize = new AtomicLong(2048L);

        // Mock static Files.size method
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.size(path)).thenReturn(1024L);

            // Use reflection to set private maps
            String key = getKeyForTest(serverSession, path);
            setPrivateField(customSFTPEventListener, "fileWriteSizes", key, mockWriteSize);
            setPrivateField(customSFTPEventListener, "fileReadSizes", key, mockReadSize);
            setPrivateField(customSFTPEventListener, "fileAccessed", key, true);

            customSFTPEventListener.closed(serverSession, "remoteHandle", handle, null);

            // Verify log output
            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList).isNotEmpty();
            assertThat(logsList.get(0).getFormattedMessage()).contains("wrote to file");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setPrivateField(Object target, String fieldName, String key, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Map<String, Object> map = (Map<String, Object>) field.get(target);
        map.put(key, value);
    }

    // Assuming getKey is something like this; adjust as needed
    private String getKeyForTest(ServerSession session, Path path) {
        return session.getUsername() + "-" + path.toString();
    }

    @Test
    @DisplayName("Test removing method log")
    public void testRemoving() throws IOException {
        when(handle.getFile()).thenReturn(path);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.size(path)).thenReturn(1024L); // Mock file size

            customSFTPEventListener.removing(serverSession, path, false);

            // Verify log output
            List<ILoggingEvent> logsList = listAppender.list;
            logsList.forEach(event -> System.out.println(event.getFormattedMessage())); // For debugging
            assertThat(logsList).isNotEmpty();
            assertThat(logsList.get(0).getFormattedMessage()).contains("is deleting file");
        }
    }

    @Test
    @DisplayName("Test removed method log")
    public void testRemoved() throws IOException {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.size(path)).thenReturn(1024L); // Mock file size

            customSFTPEventListener.removed(serverSession, path, false, null);

            // Verify log output
            List<ILoggingEvent> logsList = listAppender.list;
            assertThat(logsList).isNotEmpty();
            assertThat(logsList.get(0).getFormattedMessage()).contains("successfully deleted file");
        }
    }

    @Test
    @DisplayName("Test created method log")
    public void testCreated() throws IOException {
        customSFTPEventListener.created(serverSession, path, null, null);

        // Verify log output
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).isNotEmpty();
        assertThat(logsList.get(0).getFormattedMessage()).contains("successfully created directory");
    }
}