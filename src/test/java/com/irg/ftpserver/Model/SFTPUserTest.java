package com.irg.ftpserver.Model;

import com.irg.ftpserver.model.SFTPPublicKey;
import com.irg.ftpserver.model.SFTPUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SFTPUserTest {
    @Test
    @DisplayName("Test SFTPUser Model")
    public void testUserModel() {
        // Create an empty list of SFTPPublicKey
        List<SFTPPublicKey> publicKeys = new ArrayList<>();

        // Create an SFTPUser instance using the parameterized constructor
        SFTPUser sftpUser = new SFTPUser(
                UUID.randomUUID(), // Generate a random ID
                "testUser",
                "password",
                "/home/testUser",
                new Date(), // createdDate
                1, // companyId
                "TestCompany", // companyName
                publicKeys, // SFTPPublicKeys
                "http://example.com/ticket", // ticketUrl
                true, // enabled
                null, // lastLoginDate
                false, // passwordLoginEnabled
                false, // publicKeyLoginEnabled
                null, // lastPasswordChange
                new Date(), // modifiedDate
                new ArrayList<>() // publicKeysFromConfig
        );

        // Assertions
        Assertions.assertEquals("testUser", sftpUser.getUsername());
        Assertions.assertEquals("password", sftpUser.getPassword());
        Assertions.assertEquals("/home/testUser", sftpUser.getDirectory());

        // Create another SFTPUser for comparison
        SFTPUser anotherSFTPUser = new SFTPUser(
                UUID.randomUUID(), // Generate a random ID
                "testUser",
                "password",
                "/home/testUser",
                new Date(),
                1,
                "TestCompany",
                publicKeys,
                "http://example.com/ticket",
                true,
                null,
                false,
                false,
                null,
                new Date(),
                new ArrayList<>()
        );

        Assertions.assertEquals(sftpUser, anotherSFTPUser);
        Assertions.assertEquals(sftpUser.hashCode(), anotherSFTPUser.hashCode());
        Assertions.assertEquals(sftpUser.toString(), anotherSFTPUser.toString());
    }
}
