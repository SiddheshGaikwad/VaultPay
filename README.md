# VaultPay – Digital Banking & Ledger System

VaultPay is a backend digital banking application built using Java and Spring Boot. It provides REST APIs for user management, account operations, transactions, fund transfers, and ledger entries, with JWT-based authentication and role-based authorization.

## Features

- **User Management** – Create users and authenticate using login APIs.
- **Account Management** – Create and retrieve bank accounts.
- **Transactions** – Manage deposits, withdrawals, and transaction history.
- **Fund Transfers** – Transfer funds between accounts.
- **Double-Entry Ledger** – Maintain debit and credit ledger entries.
- **Security** – JWT authentication and role-based access control for customers and admins.
- **Exception Handling** – Centralized exception handling with meaningful API responses.
- **API Documentation** – Swagger UI using SpringDoc OpenAPI.
- **Monitoring** – Spring Boot Actuator health and info endpoints.
- **Unit Testing** – JUnit and Mockito tests for controllers, services, security, and exception handling.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Backend programming |
| Spring Boot | Application framework |
| Spring Data JPA / Hibernate | Database operations and ORM |
| MySQL | Relational database |
| Spring Security | Authentication and authorization |
| JWT | Token-based authentication |
| Maven | Dependency management and build |
| JUnit / Mockito | Automated testing |
| Swagger / OpenAPI | API documentation |
| Spring Boot Actuator | Application monitoring |

## Architecture

The application follows a layered architecture:

- **Controller Layer** – Handles HTTP requests and responses.
- **Service Layer** – Implements business logic.
- **Repository Layer** – Communicates with the database.
- **Entity Layer** – Represents database tables.
- **DTO Layer** – Transfers request and response data.
- **Security Layer** – Handles JWT authentication and access control.

## Prerequisites

- Java 17
- MySQL 8.x
- Maven (or the included Maven Wrapper)
- Git

## Configuration

Configure the following environment variables before running the application:

```text
DB_PASSWORD=your_mysql_password
VAULTPAY_JWT_SECRET=your_secure_jwt_secret
```

The JWT secret must be sufficiently strong and must be kept private.

Check `src/main/resources/application.properties` to confirm the database URL and other application settings.

## Run Locally

**1. Clone the repository**

```bash
git clone https://github.com/SiddheshGaikwad/VaultPay.git
cd VaultPay
```

**2. Configure environment variables**

Set `DB_PASSWORD` and `VAULTPAY_JWT_SECRET` in your operating system or IDE run configuration.

**3. Start the application**

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

**4. Access the application**

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Actuator Health: `http://localhost:8080/actuator/health`
- Actuator Info: `http://localhost:8080/actuator/info`

Ensure MySQL is running and the configured database settings are correct.

## Testing

Run the automated test suite using the Maven Wrapper:

```cmd
mvnw.cmd test
```

## Security Notes

- Never commit database passwords, JWT secrets, or other credentials.
- Use a unique, strong JWT secret in each environment.
- Configure production database credentials securely.
- Review security settings before deploying the application.

## Future Enhancements

- React.js frontend with Tailwind CSS
- Enhanced transaction reporting and dashboard
- Docker-based deployment
- Additional integration and security tests

## Author

**Siddhesh Gaikwad**

GitHub: [SiddheshGaikwad](https://github.com/SiddheshGaikwad)
