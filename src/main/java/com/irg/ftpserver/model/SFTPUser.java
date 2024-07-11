package com.irg.ftpserver.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data  // Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor  // Generates default constructor
@AllArgsConstructor  // Generates constructor with all arguments
@Table(name = "sftp_users", schema = "ftpserver")
public class SFTPUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, name = "username")
    @NotNull
    @Size(min = 3, max = 25)
    private String username;

    @Column(nullable = true, name = "password")
    @Size(min = 60, max = 100)
    private String password;

    private String directory;

    @Column(nullable = false, name="created_at")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "Created Date is required")
    private Date createdDate;

    @Column(nullable = false, name = "company_id")
    @NotNull(message = "Company ID is required")
    @Min(1) // Minimum 5-digit integer
    @Max(99999) // Maximum 5-digit integer
    private int companyId;

    @Column(nullable = false, name = "company_name")
    @NotNull(message = "Company Name is required")
    @Size(min = 3, max = 25)
    private String companyName;

    @OneToMany(mappedBy = "sftpUser", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PublicKey> publicKeys = new ArrayList<>();

    @Column(nullable = false, name = "ticket_url")
    @URL
    @NotNull(message = "Ticket URL is required")
    private String ticketUrl;

    @Column(nullable = false, name = "enabled")
    @NotNull(message = "Enabled is required")
    private boolean enabled;

    @Column(nullable = true, name="last_login")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginDate;

    @Column(nullable = false, name="password_login_enabled")
    private boolean passwordLoginEnabled;

    @Column(nullable = false, name="public_key_login_enabled")
    private boolean publicKeyLoginEnabled;

    @Column(nullable = true, name="last_password_change")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPasswordChange;

    @Transient
    private List<String> publicKeysFromConfig = new ArrayList<>();
}
