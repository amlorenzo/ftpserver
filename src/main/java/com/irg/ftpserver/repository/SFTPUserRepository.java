package com.irg.ftpserver.repository;

import com.irg.ftpserver.model.SFTPUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SFTPUserRepository extends JpaRepository<SFTPUser, UUID> {
    Optional<SFTPUser> findByUsername(String username);

}
