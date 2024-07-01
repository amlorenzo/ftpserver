package com.irg.ftpserver.events;

import org.apache.sshd.common.kex.KexProposalOption;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionListener;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/************************************************************
 * CustomSessionListener
 * Custom Session Listener to log session events
 ************************************************************/
@Component
public class CustomSFTPSessionListener implements SessionListener {

    private static final Logger log = LoggerFactory.getLogger(CustomSFTPSessionListener.class);

    @Override
    public void sessionCreated(Session session) {
        log.info("Session created: {} by User: {}", session, session.getUsername());
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
        log.info("Session closed: {} by User: {}, from: {}", session, session.getUsername()
                , session.getIoSession().getRemoteAddress());
    }
}
