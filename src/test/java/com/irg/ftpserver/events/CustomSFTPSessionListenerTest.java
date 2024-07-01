package com.irg.ftpserver.events;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.irg.ftpserver.TestLogAppender;
import org.apache.sshd.common.io.IoSession;
import org.apache.sshd.common.kex.KexProposalOption;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.server.session.ServerSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CustomSFTPSessionListenerTest {

    private CustomSFTPSessionListener customSFTPSessionListener;
    private Session session;
    private IoSession ioSession;
    private ServerSession serverSession;
    private TestLogAppender testLogAppender;

    @BeforeEach
    public void setUp() {
        customSFTPSessionListener = new CustomSFTPSessionListener();
        session = Mockito.mock(Session.class);
        ioSession = Mockito.mock(IoSession.class);
        serverSession = Mockito.mock(ServerSession.class);

        // Mock the IoSession to return a valid InetSocketAddress
        when(ioSession.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 2221));
        when(session.getIoSession()).thenReturn(ioSession);
        when(session.getUsername()).thenReturn("testUser");

        // Mock serverSession for sessionEvent test
        when(serverSession.getIoSession()).thenReturn(ioSession);
        when(serverSession.getUsername()).thenReturn("testUser");
        when(serverSession.getNegotiatedKexParameter(KexProposalOption.C2SENC)).thenReturn("aes128-ctr");
        when(serverSession.getNegotiatedKexParameter(KexProposalOption.C2SMAC)).thenReturn("hmac-sha2-256");
        when(serverSession.getNegotiatedKexParameter(KexProposalOption.ALGORITHMS))
                .thenReturn("diffie-hellman-group14-sha1");
        when(serverSession.getClientVersion()).thenReturn("SSH-2.0-OpenSSH_7.4");

        // Add custom log appender
        Logger logger = (Logger) LoggerFactory.getLogger(CustomSFTPSessionListener.class);
        testLogAppender = new TestLogAppender();
        logger.addAppender(testLogAppender);
        testLogAppender.start();
    }

    @Test
    @DisplayName("Test sessionCreated method log")
    public void testSessionCreated() {
        customSFTPSessionListener.sessionCreated(session);

        List<ILoggingEvent> logEvents = testLogAppender.getEvents();
        assertEquals(1, logEvents.size());
        ILoggingEvent logEvent = logEvents.get(0);
        assertEquals("Session created: Mock for Session, hashCode: " + session.hashCode()
                + " by User: testUser", logEvent.getFormattedMessage());
        assertEquals("testUser", session.getUsername());
    }

    @Test
    @DisplayName("Test sessionEvent method log")
    public void testSessionEvent() {
        customSFTPSessionListener.sessionEvent(serverSession, SessionListener.Event.Authenticated);

        List<ILoggingEvent> logEvents = testLogAppender.getEvents();
        assertEquals(1, logEvents.size());
        ILoggingEvent logEvent = logEvents.get(0);
        assertEquals("Session Authenticated: Mock for ServerSession, hashCode: " + serverSession.hashCode() +
                        ", by User: testUser, from IP: /127.0.0.1:2221, with Client Version: SSH-2.0-OpenSSH_7.4, " +
                        "with Cipher: aes128-ctr, MAC: hmac-sha2-256, Algo: diffie-hellman-group14-sha1",
                logEvent.getFormattedMessage());
        assertEquals("testUser", serverSession.getUsername());
        assertEquals("/127.0.0.1:2221", serverSession.getIoSession().getRemoteAddress().toString());
        assertEquals("SSH-2.0-OpenSSH_7.4", serverSession.getClientVersion());
        assertEquals("aes128-ctr", serverSession.getNegotiatedKexParameter(KexProposalOption.C2SENC));
        assertEquals("hmac-sha2-256", serverSession.getNegotiatedKexParameter(KexProposalOption.C2SMAC));
        assertEquals("diffie-hellman-group14-sha1",
                serverSession.getNegotiatedKexParameter(KexProposalOption.ALGORITHMS));
    }

    @Test
    @DisplayName("Test sessionClosed method log")
    public void testSessionClosed() {
        customSFTPSessionListener.sessionClosed(session);
        List<ILoggingEvent> logEvents = testLogAppender.getEvents();
        assertEquals(1, logEvents.size());
        ILoggingEvent logEvent = logEvents.get(0);
        assertEquals("Session closed: Mock for Session, hashCode: " + session.hashCode() +
                " by User: testUser, from: /127.0.0.1:2221", logEvent.getFormattedMessage());
        assertEquals("testUser", session.getUsername());
        assertEquals("/127.0.0.1:2221", session.getIoSession().getRemoteAddress().toString());
    }
}