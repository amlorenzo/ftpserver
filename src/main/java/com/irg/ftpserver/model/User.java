package com.irg.ftpserver.model;

import com.irg.ftpserver.data.Role;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

@Entity
@Data  // Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor  // Generates default constructor
@AllArgsConstructor  // Generates constructor with all arguments
@Table(name = "useraccounts", schema = "ftpserver")
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, name = "username")
    @NotNull(message = "Username is required")
    @Size(min = 5, max = 25)
    private String username;

    @ToString.Exclude
    @Column(nullable = false, name = "password")
    @NotNull(message = "Password is required")
    @Size(min = 60, max = 100)
    private String password;

    @Column(nullable = false, unique = true, name = "email")
    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(nullable = false, name="created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Created Date is required")
    private Date createdDate;

    @Column(nullable = false, name = "enabled")
    @NotNull(message = "Enabled is required")
    private boolean enabled;

    //Assigned default value to role
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    @NotNull(message = "Role is required")
    private Role role = Role.USER;

    @Column(nullable = false, name = "company_name")
    @NotNull(message = "Company Identifier is required")
    @Size(min = 3, max = 25)
    private String companyName;

    @Column(nullable = false, name = "first_login")
    @NotNull(message = "First Login is required")
    private boolean firstLogin = true;

    @Column(name="last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginDate;

    @Column(name = "last_password_change")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPasswordChange;

    @Column(nullable = false, name = "modified_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.asAuthority()));
    }

}
