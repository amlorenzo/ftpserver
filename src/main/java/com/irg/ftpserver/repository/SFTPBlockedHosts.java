package com.irg.ftpserver.repository;

import com.irg.ftpserver.model.SFTPBlockedHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SFTPBlockedHosts extends JpaRepository<SFTPBlockedHost, Long>{
}
