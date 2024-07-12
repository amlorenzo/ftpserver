package com.irg.ftpserver.repository;

import com.irg.ftpserver.model.SFTPBlockedHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SFTPBlockedHostsRepository extends JpaRepository<SFTPBlockedHost, Long>{
    Optional<SFTPBlockedHost> findByIpAddress(String ipAddress);
    Optional<SFTPBlockedHost> findByIpAddressAndAllowFalse(String ipAddress);
}
