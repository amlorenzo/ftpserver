-- V1__Create_SFTPUser_table.sql

CREATE TABLE IF NOT EXISTS sftp_user (
     id UUID PRIMARY KEY,
     username VARCHAR(25) NOT NULL UNIQUE,
     password VARCHAR(255),
     directory VARCHAR(255),
     company_id INT NOT NULL,
     public_key TEXT,
     ticket_url VARCHAR(255) NOT NULL
    );