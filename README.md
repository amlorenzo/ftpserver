# FTP Server

This project is an SFTP server using Apache MINA SSHD and Spring Boot.

## Table of Contents

1. [Project Overview](#project-overview)
    - [Introduction](#introduction)
    - [Features](#features)
    - [Technologies Used](#technologies-used)
2. [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Configuration](#configuration)
3. [Usage](#usage)
    - [Running the Application](#running-the-application)
    - [Command Line Interface](#command-line-interface)
4. [Architecture](#architecture)
    - [High-Level Architecture](#high-level-architecture)
    - [Modules](#modules)
5. [Services](#services)
    - [BlockedHostService](#blockedhostservice)
    - [SFTPPasswordLoginService](#sftppasswordloginservice)
    - [SFTPPublicKeyLoginService](#sftppublickeyloginservice)
6. [Event Handling](#event-handling)
    - [CustomSFTPEventListener](#customsftpeventlistener)
    - [HostBlockedEvent](#hostblockedevent)
7. [Configuration](#configuration)
    - [SFTPServerConfig](#sftpserverconfig)
    - [SFTPInitialConfigService](#sftpinitialconfigservice)
    - [SFTPInitialUserInitService](#sftpinitialuserinitservice)
8. [Logging](#logging)
    - [Logging Configuration](#logging-configuration)
    - [Log Formats](#log-formats)
9. [Security](#security)
    - [Authentication](#authentication)
    - [Authorization](#authorization)
10. [Testing](#testing)
    - [Unit Tests](#unit-tests)
    - [Integration Tests](#integration-tests)
11. [Deployment](#deployment)
    - [Docker](#docker)
    - [Kubernetes](#kubernetes)
12. [Troubleshooting](#troubleshooting)
    - [Common Issues](#common-issues)
    - [FAQ](#faq)
13. [Contributing](#contributing)
    - [How to Contribute](#how-to-contribute)
    - [Code of Conduct](#code-of-conduct)
14. [License](#license)
15. [Contact](#contact)

## Project Overview

### Introduction
This is an SFTP server based on the [Apache SSHD MINA library](https://mina.apache.org/sshd-project/), Spring Boot, and PostgreSQL. The fundamental idea behind the project is to provide a serverless SFTP server with Virtual File System (VFS) connectivity to cloud storage, such as AWS S3, Google Cloud Platform (GCP), and others. The project aims to offer a scalable and flexible solution for secure file transfers.

Key features include:
- **Cloud Storage Integration**: Seamless integration with various cloud storage providers, enabling users to interact with cloud files as if they were local.
- **Admin Interface**: A comprehensive administrative interface for managing users and permissions. This will include integration with cloud identity and access management (IAM) services like AWS IAM.
- **Secure Logging**: Robust logging mechanisms to ensure all actions are recorded securely. Logs are stored in a manner that prevents tampering and supports auditing.
- **Advanced Security Features**: Enhanced security measures such as IP blocking, multi-factor authentication, and detailed access controls.

Currently, the project is a work in progress. The following components have been implemented:
- **SFTP Functionality**: Basic SFTP operations using Apache MINA SSHD.
- **Security Features**: IP blocking based on failed login attempts, and support for both password and public key authentication.
- **Event Listeners**: Custom event listeners to handle various server events, providing hooks for extending functionality.
- **Database Connectivity**: Integration with PostgreSQL for user management and configuration storage.

### Pending Work
There are several key features and enhancements that are still under development:
- **REST API/GraphQL API**: Development of a RESTful API and/or GraphQL API to allow programmatic access to server functionalities, user management, and file operations.
- **Admin Interface**: Building a user-friendly web interface for administrators to manage users, view logs, and configure server settings.
- **Enhanced Logging and Auditing**: Further improvements to logging mechanisms to ensure comprehensive auditing capabilities.
- **Additional Security Enhancements**: Implementing advanced security features such as multi-factor authentication and more granular access controls.
- **VFS Connectivity**: Extending the VFS capabilities to support a wider range of cloud storage providers and ensuring seamless integration.

This project aims to deliver a robust and scalable SFTP server solution, leveraging the power of cloud storage and modern security practices to meet the needs of today's enterprises.

### Features
The project aims to provide a comprehensive SFTP server solution with the following features:

### Completed Features
- ~~**SSH Server**: The core SFTP server functionality using Apache MINA SSHD.~~
- ~~**Password Authentication**: Support for user authentication via passwords.~~
- ~~**Public Key Authentication**: Support for user authentication via public key.~~
- ~~**Logging/Event Monitoring**: Robust logging and event monitoring mechanisms to track server activity and user actions.~~
- ~~**Database Persistence**: Integration with PostgreSQL for persisting user data and server configurations.~~
- ~~**Configuration Persistence**: Storing server configurations in the database for easy management.~~

### Pending Work
- **Capability to Reload Application Context Beans**: Automatically reload application context beans after an admin makes configuration changes.
- **REST API**: Development of a RESTful API to allow programmatic access to server functionalities, user management, and file operations.
- **GraphQL API**: Development of a GraphQL API for flexible and efficient querying and manipulation of server data.
- **Admin UI**: Building a user-friendly web interface for administrators to manage users, view logs, and configure server settings.
- **Integration with AWS IAM**:
    - **Configurable IAM Settings**: Allow an admin to configure IAM settings through the Admin UI.
    - **Admin Login Using IAM Accounts**: Enable administrators to log in using their AWS IAM accounts, ensuring secure and centralized user management.
    - **IAM Role and Permission Management**: Provide an interface to manage user roles and permissions using AWS Identity and Access Management (IAM).
- **Integration with AWS S3**: Admin interface to configure and manage VFS connectivity to AWS S3 for seamless cloud storage integration.
- **Additional Security Enhancements**: Implementing advanced security features such as multi-factor authentication and more granular access controls.
- **Enhanced Logging and Auditing**: Further improvements to logging mechanisms to ensure comprehensive auditing capabilities.
- **Extended VFS Connectivity**: Support for a wider range of cloud storage providers, ensuring seamless integration with various cloud storage solutions.
- **Advanced Monitoring and Alerting**: Implementing monitoring and alerting mechanisms to notify administrators of suspicious activities or critical server events.

This section outlines the key features that the project aims to achieve, with clear distinctions between what has been completed and what is still pending.

### Technologies Used
The project leverages a combination of modern technologies and frameworks to provide a robust and scalable SFTP server solution. Below is a list of key technologies used in this project:

#### Core Technologies
- **Java 21**: The primary programming language used for the project.
- **Spring Boot 3.3.1**: Provides a robust framework for building Java-based applications, enabling easy setup and rapid development.
    - **Spring Boot Starter Web**: For building web applications and RESTful services.
    - **Spring Boot Starter Security**: For securing the application.
    - **Spring Boot Starter Data JPA**: For data persistence using JPA and Hibernate.
    - **Spring Boot Starter Validation**: For validating application data.
- **Apache MINA SSHD 2.13.1**: Used for implementing the core SFTP server functionalities.
    - **sshd-core**: Core SSHD library.
    - **sshd-sftp**: SFTP support in Apache MINA SSHD.
- **PostgreSQL**: Used as the primary database for storing user data and configurations.
    - **PostgreSQL JDBC Driver**: For connecting to the PostgreSQL database.
    - **Flyway**: For database migrations.

#### Development and Build Tools
- **Gradle**: Build automation tool used for managing dependencies and building the project.
- **Lombok**: For reducing boilerplate code in Java by generating getters, setters, and other utility methods.
- **Springfox 3.0.0**: For documenting RESTful APIs.

#### Testing
- **JUnit 5**: For unit testing and integration testing.
- **Mockito**: For mocking dependencies in tests.
- **Logback**: For logging during tests.

#### Configuration Management
- **HikariCP 5.0.1**: A high-performance JDBC connection pool.

#### Pending and Future Technologies
- **Spring Boot Starter GraphQL**: To be used for developing a GraphQL API for flexible and efficient data querying.
- **Spring Cloud**: Potentially required for dynamically reloading application context beans after configuration changes.
- **AWS SDK**: To be used for integrating with AWS services like IAM and S3.
- **Spring Cloud AWS**: For seamless integration with AWS services, enabling features like IAM configuration and S3 connectivity.
- **Admin UI Framework**: To be decided for building the web-based admin interface for managing users and configurations.

## Getting Started

### Prerequisites
- Prerequisite 1
- Prerequisite 2

### Installation
1. Clone the repository
2. Install dependencies
3. Run the application

### Configuration
- Configuration details

## Usage

### Running the Application
- How to run the application

### Command Line Interface
- Available commands and options

## Architecture

### High-Level Architecture
- Overview of the system architecture

### Modules
- Description of various modules

## Services

### BlockedHostService
- Details about BlockedHostService

### SFTPPasswordLoginService
- Details about SFTPPasswordLoginService

### SFTPPublicKeyLoginService
- Details about SFTPPublicKeyLoginService

## Event Handling

### CustomSFTPEventListener
- Details about CustomSFTPEventListener

### HostBlockedEvent
- Details about HostBlockedEvent

## Configuration

### SFTPServerConfig
- Configuration details for SFTPServer

### SFTPInitialConfigService
- Details about SFTPInitialConfigService

### SFTPInitialUserInitService
- Details about SFTPInitialUserInitService

## Logging

### Logging Configuration
- How to configure logging

### Log Formats
- Log formats used in the application

## Security

### Authentication
- Details about authentication mechanisms

### Authorization
- Details about authorization mechanisms

## Testing

### Unit Tests
- Information on unit tests

### Integration Tests
- Information on integration tests

## Deployment

### Docker
- How to deploy using Docker

### Kubernetes
- How to deploy using Kubernetes

## Troubleshooting

### Common Issues
- List of common issues and solutions

### FAQ
- Frequently Asked Questions

## Contributing

### How to Contribute
- Guidelines for contributing to the project

### Code of Conduct
- Code of conduct for contributors

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for more details.

## Contact
- Contact details for support or questions

## Authors
Alex L