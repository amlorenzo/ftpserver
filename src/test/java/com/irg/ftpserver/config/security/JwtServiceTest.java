package com.irg.ftpserver.config.security;

import com.irg.ftpserver.data.Role;
import com.irg.ftpserver.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class JwtServiceTest {


    @Autowired
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setRole(Role.valueOf("USER"));
        testUser.setEmail("test@example.com");
    }

    @Test
    public void testGenerateToken() {
        String token = jwtService.generateToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

}