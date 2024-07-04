package com.irg.ftpserver.Model;

import com.irg.ftpserver.model.SFTPUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SFTPUserTest {
    @Test
    @DisplayName("Test SFTPUser Model")
    public void testUserModel() {
        SFTPUser SFTPUser = new SFTPUser("testUser", "password", "/home/testUser");
        Assertions.assertEquals("testUser", SFTPUser.getUsername());
        Assertions.assertEquals("password", SFTPUser.getPassword());
        Assertions.assertEquals("/home/testUser", SFTPUser.getDirectory());

        SFTPUser anotherSFTPUser = new SFTPUser("testUser", "password", "/home/testUser");
        Assertions.assertEquals(SFTPUser, anotherSFTPUser);
        Assertions.assertEquals(SFTPUser.hashCode(), anotherSFTPUser.hashCode());
        Assertions.assertEquals(SFTPUser.toString(), anotherSFTPUser.toString());
    }
}
