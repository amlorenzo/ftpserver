package com.irg.ftpserver.events;

import com.irg.ftpserver.service.SFTPLoginService;
import org.apache.sshd.common.kex.KexProposalOption;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/************************************************************
 * CustomSessionListener
 * Custom Session Listener to log session events
 ************************************************************/
@Component
public class CustomSFTPSessionListener implements SessionListener {

    private static final Logger log = LoggerFactory.getLogger(CustomSFTPSessionListener.class);
    private final SFTPLoginService sftpLoginService;
    private final Map<Session, Boolean> sessionClosedFlags = new ConcurrentHashMap<>();

    @Override
    public void sessionCreated(Session session) {
        String host = ((InetSocketAddress)session.getIoSession().getRemoteAddress()).getAddress().getHostAddress();
        log.info("Session established: {}", session);
        if (sftpLoginService.isBlocked(host)){
            log.info("Blocked Host: {} tried to connect", host);
            session.close(false);
        }
    }

    @Override
    public void sessionEvent(Session session, Event event) {
        if (event == Event.Authenticated){
            if(session instanceof ServerSession serverSession){
                String cipher = serverSession.getNegotiatedKexParameter(KexProposalOption.C2SENC);
                String mac = serverSession.getNegotiatedKexParameter(KexProposalOption.C2SMAC);
                String algo = serverSession.getNegotiatedKexParameter(KexProposalOption.ALGORITHMS);
                String clientIp = serverSession.getIoSession().getRemoteAddress().toString();
                String clientVersion = serverSession.getClientVersion();

                log.info("Session Authenticated: {}, by User: {}, from IP: {}, with Client Version: {}, with Cipher: {}" +
                                ", MAC: {}, Algo: {}",
                        session, session.getUsername(), clientIp,clientVersion, cipher, mac, algo);
            }
        }
    }

    @Override
    public void sessionClosed(Session session) {
        // Avoid duplicate session closed events
        if (sessionClosedFlags.putIfAbsent(session, true) == null) {
            log.debug("Entering sessionClosed method for session: {}", session);

            String username = session.getUsername();
            if (username == null) {
                log.info("Session closed: {} To Host: {}", session, session.getIoSession().getRemoteAddress());
            } else {
                log.info("Session closed: {} by User: {}, To Host: {}", session, session.getUsername()
                        , session.getIoSession().getRemoteAddress());
            }
            log.debug("Exiting sessionClosed method for session: {}", session);
        }
    }

    public CustomSFTPSessionListener(SFTPLoginService sftpLoginService) {
        this.sftpLoginService = sftpLoginService;
    }
}
