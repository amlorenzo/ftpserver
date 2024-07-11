package com.irg.ftpserver.model;

import com.irg.ftpserver.validation.ValidRSAPublicKey;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "public_keys", schema = "ftpserver")
@Data
@NoArgsConstructor
public class PublicKey {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(columnDefinition = "text", nullable = true, name = "public_key")
    @ValidRSAPublicKey(minKeyLength = 2048, message = "Invalid RSA Public Key")
    private String publicKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sftp_user_id", nullable = false)
    private SFTPUser sftpUser;

    @Column(nullable = false, name = "created_at")
    private Date createdDate;

}
