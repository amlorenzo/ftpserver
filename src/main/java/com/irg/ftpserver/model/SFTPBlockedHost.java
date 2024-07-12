package com.irg.ftpserver.model;

import com.irg.ftpserver.validation.ValidIpAddress;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sftp_blocked_hosts", schema = "ftpserver")
public class SFTPBlockedHost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, name = "ip_address")
    @ValidIpAddress
    private String IpAddress;

    @Column(nullable = false, name = "reason")
    @Size(min = 5, max = 255)
    private String reason;

    @Column(nullable = false, name = "username_attempted")
    @Size(max = 255)
    private String username;

    @Column(nullable = false, name = "blocked_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date blockedAt;

    @Column(nullable = false, name = "allow")
    private boolean allow;
}
