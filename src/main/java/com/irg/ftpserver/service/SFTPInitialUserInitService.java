package com.irg.ftpserver.service;

import com.irg.ftpserver.config.SFTPServerProperties;
import com.irg.ftpserver.data.Role;
import com.irg.ftpserver.model.SFTPUser;
import com.irg.ftpserver.model.User;
import com.irg.ftpserver.repository.SFTPUserRepository;
import com.irg.ftpserver.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Data
@AllArgsConstructor
public class SFTPInitialUserInitService {
    
    private static final Logger logger = LoggerFactory.getLogger(SFTPInitialUserInitService.class);
    
    private UserRepository userRepository;
    
    private SFTPUserRepository sftpUserRepository;
    
    private PasswordEncoder passwordEncoder;
    
    private SFTPServerProperties sftpServerProperties;
    
    @PostConstruct
    public void initializeDefaultUsers() {
        createDefaultAdminUser();
        createDefaultSFTPUser();
    }

    private void createDefaultSFTPUser() {
        if(sftpUserRepository.findByUsername("testuser").isEmpty()){
            SFTPUser sftpUser = new SFTPUser();
            sftpUser.setUsername(sftpServerProperties.getSFTPUsers().getFirst().getUsername());
            String password = sftpServerProperties.getSFTPUsers().getFirst().getPassword();
            sftpUser.setPassword(passwordEncoder.encode(password));
            sftpUser.setDirectory(sftpServerProperties.getSFTPUsers().getFirst().getDirectory());
            sftpUser.setCreatedDate(new Date());
            sftpUser.setCompanyId(1);
            sftpUser.setCompanyName(sftpServerProperties.getSFTPUsers().getFirst().getCompanyName());
            sftpUser.setTicketUrl(sftpServerProperties.getSFTPUsers().getFirst().getTicketUrl());
            sftpUser.setEnabled(true);
            logger.info(sftpServerProperties.getSFTPUsers().getFirst().getPublicKey());
            sftpUser.setPublicKey(sftpServerProperties.getSFTPUsers().getFirst().getPublicKey());
            sftpUserRepository.save(sftpUser);
            logger.info("User {} created successfully with password: {}",sftpUser.getUsername(),password);
            logger.info("Verifying passwords match for user: testuser: {}",
                    passwordEncoder.matches(password, sftpUser.getPassword()) ? "Match" : "No Match");
        }
    }

    private void createDefaultAdminUser() {
        if(userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername(sftpServerProperties.getUsers().getFirst().getUsername());
            String password = sftpServerProperties.getUsers().getFirst().getPassword();
            admin.setPassword(passwordEncoder.encode(password));
            admin.setCreatedDate(new Date());
            admin.setRole(Role.Admin);
            admin.setCompanyName(sftpServerProperties.getUsers().getFirst().getCompanyName());
            userRepository.save(admin);
            logger.info("User {} created successfully with password: {}",admin.getUsername(), password);
            logger.info("Verifying passwords match for user: admin: {}",
                    passwordEncoder.matches(password, admin.getPassword()) ? "Match" : "No Match");

        } else {
            logger.info("User Admin already exists, skipping creation.");
        }
    }


}
