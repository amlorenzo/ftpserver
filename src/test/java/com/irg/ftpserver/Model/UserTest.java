package com.irg.ftpserver.Model;

import com.irg.ftpserver.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserTest {
    @Test
    @DisplayName("Test User Model")
    public void testUserModel() {
        User user = new User("testUser", "password", "/home/testUser");
        Assertions.assertEquals("testUser", user.getUsername());
        Assertions.assertEquals("password", user.getPassword());
        Assertions.assertEquals("/home/testUser", user.getDirectory());

        User anotherUser = new User("testUser", "password", "/home/testUser");
        Assertions.assertEquals(user, anotherUser);
        Assertions.assertEquals(user.hashCode(), anotherUser.hashCode());
        Assertions.assertEquals(user.toString(), anotherUser.toString());
    }
}
