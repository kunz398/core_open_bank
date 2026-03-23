# Open Source Core Banking System (OSCBS)

> A fully open-source core banking platform built for modern financial institutions.
OSCBS aims to provide a complete, production-grade core banking system covering everything from teller operations and treasury management to ATM connectivity, loans, and payment gateway integrations — all open source.

> This project would also contain an ATM simulation with HSM integrated

---



## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.3 |
| Language | Java 21 |
| Database | PostgreSQL + Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Password Hashing | Argon2 via Bouncy Castle |
| Validation | Jakarta Validation |
| Boilerplate | Lombok |
| Config | `.env` via environment variables |

---


### Prerequisites

- Java 21
- PostgreSQL 17+
- Maven 3.9+

### 1. Clone the repository

```bash
git clone https://github.com/your-org/oscbs.git
cd oscbs
```

### 2. Create your `.env` file
> you should use the `.env.example` as a base.

### 3. Run the database schema

```bash
psql -U your_db_user -d oscbs -f schema.sql
```

### 4. Start the application

```bash
mvn spring-boot:run
```

On first run the bootstrap service will automatically create the first admin user using your `BOOTSTRAP_*` env vars.

---

## Authentication

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "admin",
  "password": "password_for_admin"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```


## Modules

| Module | Status  | Description |
|---|---------|---|
| `AUTH` | Active  | Authentication & authorization |
| `ACCOUNTS` | Planned | Account management |
| `TRANSACTIONS` | Planned | Deposits, withdrawals, transfers |
| `CARDS` | Planned | Card issuance & management |
| `LOANS` | Planned | Loan origination & repayment |
| `ATM` | Planned | ATM gateway & HSM simulation |
| `FOREX` | Planned | Foreign exchange |
| `AUDIT` | Active  | Audit & reporting |
| `HSM` | Planned | Cryptographic services |

---

## Contributing

Contributions are welcome! Please open an issue first to discuss what you would like to change.

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.