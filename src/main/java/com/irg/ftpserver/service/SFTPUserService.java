package com.irg.ftpserver.service;

import com.irg.ftpserver.model.SFTPUser;
import com.irg.ftpserver.repository.SFTPUserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.util.Optional;

@Service
@AllArgsConstructor
@DependsOn({"SFTPInitialConfigService", "SFTPInitialUserInitService"})
public class SFTPUserService {

    private static final Logger logger = LoggerFactory.getLogger(SFTPUserService.class);

    private final SFTPUserRepository sftpUserRepository;

    public Optional<SFTPUser> getUserByUserName(String userName) {

        Optional<SFTPUser> sftpUser = sftpUserRepository.findByUsername(userName);
        if(sftpUser.isEmpty()){
            logger.error("User not found in repository with username: " + userName);
            throw new RuntimeException("User not found in repository with username: " + userName);
        }
        return sftpUser;
    }

}
