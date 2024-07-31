package com.irg.ftpserver.model;

import com.irg.ftpserver.validation.ValidRSAPublicKey;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "public_keys", schema = "ftpserver")
@Data
@NoArgsConstructor
@ToString
public class SFTPPublicKey {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ToString.Exclude
    @Column(columnDefinition = "text", name = "public_key")
    @ValidRSAPublicKey(minKeyLength = 2048, message = "Invalid RSA Public Key")
    private String publicKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sftp_user_id", nullable = false)
    private SFTPUser sftpUser;

    @Column(nullable = false, name = "enabled")
    private boolean enabled;

    @Column(nullable = false, name = "created_at")
    private Date createdDate;

    @Column(nullable = false, name = "modified_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;

}
