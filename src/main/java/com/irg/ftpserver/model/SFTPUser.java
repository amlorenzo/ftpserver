package com.irg.ftpserver.model;

import com.irg.ftpserver.validation.ValidRSAPublicKey;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.UUID;

@Entity
@Data  // Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor  // Generates default constructor
@AllArgsConstructor  // Generates constructor with all arguments
public class SFTPUser {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    @NotNull
    @Size(min = 3, max = 25)
    private String username;

    @Column(nullable = true)
    private String password;


    private String directory;

    @Column(nullable = false)
    @NotNull
    @Min(1) // Minimum 5-digit integer
    @Max(99999) // Maximum 5-digit integer
    private int companyId;

    @Lob
    @Column(nullable = true)
    @ValidRSAPublicKey
    private String publicKey;
}
