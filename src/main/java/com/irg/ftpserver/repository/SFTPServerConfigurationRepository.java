package com.irg.ftpserver.repository;

import com.irg.ftpserver.model.SFTPServerConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SFTPServerConfigurationRepository extends JpaRepository<SFTPServerConfiguration, UUID>{
}
