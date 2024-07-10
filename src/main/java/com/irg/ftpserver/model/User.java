package com.irg.ftpserver.model;

import com.irg.ftpserver.data.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data  // Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor  // Generates default constructor
@AllArgsConstructor  // Generates constructor with all arguments
@Table(name = "useraccounts", schema = "ftpserver")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, name = "username")
    @NotNull(message = "Username is required")
    @Size(min = 5, max = 25)
    private String username;

    @Column(nullable = false, name = "password")
    @NotNull(message = "Password is required")
    @Size(min = 60, max = 100)
    private String password;

    @Column(nullable = false, name="created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Created Date is required")
    private Date createdDate;

    @Column(nullable = false, name = "enabled")
    @NotNull(message = "Enabled is required")
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    @NotNull(message = "Role is required")
    private Role role;

    @Column(nullable = false, name = "company_name")
    @NotNull(message = "Company Identifier is required")
    @Size(min = 3, max = 25)
    private String companyName;

    @Column(nullable = false, name = "first_login")
    @NotNull(message = "First Login is required")
    private boolean firstLogin = true;

    @Column(nullable = true, name="last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginDate;

}
