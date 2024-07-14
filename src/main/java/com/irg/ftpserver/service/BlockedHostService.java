package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPBlockedHost;
import com.irg.ftpserver.repository.SFTPBlockedHostsRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@AllArgsConstructor
@Data
public class BlockedHostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockedHostService.class);

    private final SFTPBlockedHostsRepository sftpBlockedHostsRepository;

    public void recordFailedAttempt(String ipAddress, String reason) {

        Date blockedAt = new Date();
        boolean notAllow = false;

        Optional<SFTPBlockedHost> existingEntry = sftpBlockedHostsRepository.findByIpAddress(ipAddress);

        if (existingEntry.isPresent()){
            SFTPBlockedHost blockedHost = existingEntry.get();
            blockedHost.setReason(reason);
            blockedHost.setBlockedAt(blockedAt);
            blockedHost.setAllow(notAllow);
            sftpBlockedHostsRepository.save(blockedHost);
        } else {
            SFTPBlockedHost blockedHost = new SFTPBlockedHost(null, ipAddress, reason, blockedAt, notAllow);
            sftpBlockedHostsRepository.save(blockedHost);
        }
    }

    public boolean isBlocked (String ipAddress) {

        Optional<SFTPBlockedHost> blockedHost = sftpBlockedHostsRepository.findByIpAddress(ipAddress);
        return blockedHost.isPresent() && !blockedHost.get().isAllow();
    }

    public void unblock(String ipAddress) {
        Optional<SFTPBlockedHost> blockedHost = sftpBlockedHostsRepository.findByIpAddress(ipAddress);
        if (blockedHost.isPresent()) {
            SFTPBlockedHost host = blockedHost.get();
            host.setAllow(true);
            sftpBlockedHostsRepository.save(host);
        }
    }
}
