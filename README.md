# Library Management System

REST API for managing library books, borrowing, and user accounts. Built with Spring Boot 4 and JWT authentication.

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.6 / Java 17 |
| Security | Spring Security + JJWT 0.12.6 (stateless JWT) |
| Persistence | Spring Data JPA + MySQL |
| API Docs | SpringDoc OpenAPI 2.8.0 (Swagger UI) |
| Utilities | Lombok |

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8+
- Maven (or use the included `mvnw`)

### Database Setup

```sql
CREATE DATABASE library_db;
```

### Run

```bash
./mvnw spring-boot:run
```

The application seeds initial data on first startup (see [Seed Data](#seed-data)).

### Environment Variables

All have defaults for local development:

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `library_db` | Database name |
| `DB_USERNAME` | `root` | DB username |
| `DB_PASSWORD` | `root` | DB password |
| `JWT_SECRET` | *(256-bit hex)* | HMAC-SHA256 signing key — **replace in production** |
| `JWT_EXPIRATION_MS` | `900000` | Access token TTL (15 minutes) |
| `REFRESH_EXPIRATION_MS` | `604800000` | Refresh token TTL (7 days) |
| `BORROW_PERIOD_DAYS` | `14` | Days before a borrowed book is overdue |
| `FINE_PER_DAY` | `5000` | Fine per overdue day (VND) |

### Build

```bash
./mvnw clean package -DskipTests
```

### Run Tests

```bash
./mvnw test
```

---

## API Reference

Swagger UI: `http://localhost:8080/swagger-ui.html`

Paste an access token from `POST /api/auth/login` into the **Authorize** button to test protected endpoints.

### Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Self-register (always READER role) |
| POST | `/api/auth/login` | Public | Returns `{accessToken, refreshToken}` |
| POST | `/api/auth/refresh` | Public | Rotates refresh token, returns new pair |
| POST | `/api/auth/logout` | Any | Revokes refresh token |

### Books

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/books` | ADMIN | Create book + N copies |
| GET | `/api/books` | Any | Paginated list with filters |

**GET /api/books query params:** `title`, `author`, `isbn` (partial match), `page` (default 0), `size` (default 10), `sort` (default `title`), `dir` (`asc`/`desc`)

### Borrow & Return

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/borrow` | ADMIN | Borrow a book copy for a user |
| POST | `/api/return` | ADMIN | Return a borrowed book |

**POST /api/borrow body:** `{ "userId": 1, "bookId": 1 }`

**POST /api/return body:** `{ "borrowRecordId": 1 }`

### Borrow Records

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/borrow-records` | ADMIN | Paginated list with filters |
| GET | `/api/borrow-records/{id}` | ADMIN | Single record by ID |
| GET | `/api/borrow-records/my` | Any | Current user's own history |

**GET /api/borrow-records query params:** `userId`, `bookId`, `status` (`BORROWING`/`RETURNED`), `borrowDateFrom`, `borrowDateTo` (`yyyy-MM-dd`), `page`, `size`, `sort` (default `borrowDate`), `dir` (default `desc`)

### Users

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/users` | ADMIN | Paginated user list |
| POST | `/api/users` | ADMIN | Create user with any role |
| PATCH | `/api/users/{id}/deactivate` | ADMIN | Deactivate account |

### Dashboard

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/stats` | ADMIN | Total books, copies, overdue count, user count |

---

## Response Shape

All endpoints return:

```json
{
  "success": true,
  "message": "...",
  "data": { ... }
}
```

Paginated endpoints wrap `data` in:

```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

`BorrowRecordResponse` includes computed fields:

```json
{
  "dueDate": "2026-06-24",
  "overdue": false,
  "fineAmount": 0
}
```

Fine = `(overdue days) × 5000 VND`. Applies to both active borrows past due date and late returns.

---

## Roles & Permissions

| Action | READER | ADMIN |
|---|---|---|
| Browse book catalog | ✓ | ✓ |
| View own borrow history | ✓ | ✓ |
| Borrow / Return books | — | ✓ |
| Create books | — | ✓ |
| View all borrow records | — | ✓ |
| Manage users | — | ✓ |
| Dashboard stats | — | ✓ |

---

## Seed Data

Created on first startup (skipped if users already exist):

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `reader1` | `reader123` | READER |
| `reader2` | `reader123` | READER |

Books: *Clean Code* (2 copies), *Spring in Action* (2 copies), *Domain-Driven Design* (1 copy).

---

## Domain Model

```
User ──< BorrowRecord >── BookCopy >── Book
User ──< RefreshToken
```

| Entity | Key fields |
|---|---|
| `User` | username, password (BCrypt), role, active |
| `Book` | title, author, isbn |
| `BookCopy` | book, status (`AVAILABLE`/`BORROWED`) |
| `BorrowRecord` | user, bookCopy, borrowDate, dueDate, returnDate, status, fineAmount |
| `RefreshToken` | user (1:1), token (UUID), expiresAt |

---

## Project Structure

```
src/main/java/.../
├── config/         # SecurityConfig, JwtUtil, JwtAuthenticationFilter, DataSeeder
├── controller/     # AuthController, BookController, BorrowController,
│                   # BorrowRecordController, UserController, StatsController
├── dto/
│   ├── request/    # LoginRequest, CreateBookRequest, BorrowRequest, ...
│   └── response/   # ApiResponse, PageResponse, BookResponse, ...
├── entity/         # User, Book, BookCopy, BorrowRecord, RefreshToken
├── exception/      # GlobalExceptionHandler + custom exceptions
├── repository/     # JPA repos + specifications
└── service/        # Interfaces + impl/
```
