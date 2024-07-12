-- V2__Create_USER_Table.sql

CREATE TABLE IF NOT EXISTS useraccounts (
    id UUID PRIMARY KEY,
    username VARCHAR(25) NOT NULL UNIQUE,
    password VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    role VARCHAR(25) NOT NULL,
    company_name VARCHAR(25) NOT NULL,
    first_login BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    last_password_change TIMESTAMP NULL,
    modified_date TIMESTAMP NOT NULL
);