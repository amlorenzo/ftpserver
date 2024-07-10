package com.irg.ftpserver.service;

import com.irg.ftpserver.config.SFTPServerProperties;
import com.irg.ftpserver.model.User;
import com.irg.ftpserver.repository.UserRepository;
import lombok.AllArgsConstructor;
import com.irg.ftpserver.exception.PasswordChangeRequiredException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;


@Service
@AllArgsConstructor
public class UserService {

    private PasswordEncoder passwordEncoder;

    private UserRepository userRepository;

    private SFTPServerProperties sftpServerProperties;

    @Transactional(rollbackFor = {DataIntegrityViolationException.class, JpaSystemException.class})
    public boolean checkFirstLoginAndHandle(User user, String newPassword) throws PasswordChangeRequiredException {
        boolean isInitialPasswordChangeRequired = sftpServerProperties.isInitialPasswordChangeRequired();
        if (isInitialPasswordChangeRequired && user.isFirstLogin()) {
            throw new PasswordChangeRequiredException("Initial password change required");

        } else if (!user.isFirstLogin() && newPassword != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setFirstLogin(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = {DataIntegrityViolationException.class, JpaSystemException.class})
    public boolean changePassword(User user, String newPassword){
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
        userRepository.save(user);
        return true;
    }

}
