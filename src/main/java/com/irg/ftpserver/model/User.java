package com.irg.ftpserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor  // Generates default constructor
@AllArgsConstructor  // Generates constructor with all arguments
public class User {
    private String username;
    private String password;
    private String directory;
}
