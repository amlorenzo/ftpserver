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
        for (SFTPUser sftpUser : sftpServerProperties.getSFTPUsers()) {
            if (sftpUserRepository.findByUsername(sftpUser.getUsername()).isEmpty()) {
                SFTPUser user = new SFTPUser();
                user.setUsername(sftpUser.getUsername());
                String password = sftpUser.getPassword();
                user.setPassword(passwordEncoder.encode(password));
                user.setDirectory(sftpUser.getDirectory());
                user.setCreatedDate(new Date());
                user.setCompanyId(sftpUser.getCompanyId());
                user.setCompanyName(sftpUser.getCompanyName());
                user.setTicketUrl(sftpUser.getTicketUrl());
                user.setEnabled(true);
                user.setPublicKey(sftpUser.getPublicKey());
                sftpUserRepository.save(user);
                logger.info("User {} created successfully with password: {}", user.getUsername(), password);
                logger.info("Verifying passwords match for user: {}: {}",
                        passwordEncoder.matches(password, user.getPassword()) ? "Match" : "No Match");
            } else {
                logger.info("User {} already exists, skipping creation.", sftpUser.getUsername());
            }
        }
    }

        private void createDefaultAdminUser () {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername(sftpServerProperties.getUsers().getFirst().getUsername());
                String password = sftpServerProperties.getUsers().getFirst().getPassword();
                admin.setPassword(passwordEncoder.encode(password));
                admin.setCreatedDate(new Date());
                admin.setRole(Role.Admin);
                admin.setCompanyName(sftpServerProperties.getUsers().getFirst().getCompanyName());
                userRepository.save(admin);
                logger.info("User {} created successfully with password: {}", admin.getUsername(), password);
                logger.info("Verifying passwords match for user: admin: {}",
                        passwordEncoder.matches(password, admin.getPassword()) ? "Match" : "No Match");

            } else {
                logger.info("User Admin already exists, skipping creation.");
            }
        }
    }

