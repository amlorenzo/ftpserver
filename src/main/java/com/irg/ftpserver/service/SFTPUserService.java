package com.irg.ftpserver.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
@AllArgsConstructor
public class SFTPUserService {

    private PasswordEncoder passwordEncoder;

}
