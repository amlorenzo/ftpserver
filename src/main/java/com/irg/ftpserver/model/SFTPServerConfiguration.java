package com.irg.ftpserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "sftp_server_configuration", schema = "ftpserver")
@Data
@NoArgsConstructor
public class SFTPServerConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(nullable = false, name = "port")
    @NotNull
    @Min(1)
    @Max(65535)
    private int port;

    @Column(nullable = false, name = "key_path")
    @NotNull
    @Size(min = 1, max = 255)
    private String keyPath;

    @Column(nullable = false, name = "host_key_algorithm")
    @NotNull
    @Size(max=24)
    private String hostKeyAlgorithm;

    @Column(nullable = false, name = "max_write_data_packet_length")
    @NotNull
    @Min(1024)
    @Max(512000)
    private int maxWriteDataPacketLength;

    @Column(nullable = false, name = "max_login_attempt_threshold")
    @NotNull
    @Min(1)
    @Max(256)
    private int maxLoginAttemptThreshold;

    @Column(nullable = false, name = "delay_between_login_attempts")
    @NotNull
    @Min(1)
    @Max(60000)
    private int delayBetweenLoginAttempts;

    @Column(nullable = false, name = "core_pool_size")
    @NotNull
    @Min(1)
    @Max(256)
    private int corePoolSize;

    @Column(nullable = false, name = "max_pool_size")
    @NotNull
    @Min(1)
    @Max(256)
    private int maxPoolSize;

    @Column(nullable = false, name = "keep_alive_time")
    @NotNull
    @Min(1)
    @Max(60000)
    private int keepAliveTime;

    @Column(nullable = false, name = "queue_capacity")
    @NotNull
    @Min(1)
    @Max(256)
    private int queueCapacity;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "modified_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedDate;
}
