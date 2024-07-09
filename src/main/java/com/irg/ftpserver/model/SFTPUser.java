package com.irg.ftpserver.model;

import com.irg.ftpserver.validation.ValidRSAPublicKey;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;
import java.util.UUID;

@Entity
@Data  // Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor  // Generates default constructor
@AllArgsConstructor  // Generates constructor with all arguments
@Table(name = "sftp_users", schema = "ftpserver")
public class SFTPUser {
    @Id
    @GeneratedValue
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

    @Column(columnDefinition = "text", nullable = true, name = "public_key")
    @ValidRSAPublicKey(minKeyLength = 2048, message = "Invalid RSA Public Key")
    private String publicKey;

    @Column(nullable = false, name = "ticket_url")
    @URL
    @NotNull(message = "Ticket URL is required")
    private String ticketUrl;

    @Column(nullable = false, name = "enabled")
    @NotNull(message = "Enabled is required")
    private boolean enabled;
}
