package com.irg.ftpserver.events;

import lombok.Data;
import org.apache.sshd.common.session.Session;

@Data
public class HostBlockedEvent {
    private final Session session;
    private final String host;
}
