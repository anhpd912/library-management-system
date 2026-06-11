# Library Management System

REST API for managing library books, borrowing, fines, reservations, and renewals. Built with Spring Boot 4 and JWT authentication.

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

Seeds initial data on first startup (see [Seed Data](#seed-data)).

### Docker Compose

```bash
docker-compose up
```

Starts MySQL + the application together. No local MySQL install needed.

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
| `JWT_EXPIRATION_MS` | `900000` | Access token TTL (15 min) |
| `REFRESH_EXPIRATION_MS` | `604800000` | Refresh token TTL (7 days) |
| `BORROW_PERIOD_DAYS` | `14` | Loan period in days |
| `FINE_PER_DAY` | `5000` | Fine per overdue day (VND) |
| `MAX_RENEWALS` | `2` | Max renewals per loan |

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
| POST | `/api/auth/register` | Public | Self-register (READER role) |
| POST | `/api/auth/login` | Public | Returns `{accessToken, refreshToken}` |
| POST | `/api/auth/refresh` | Public | Rotates refresh token, returns new pair |
| POST | `/api/auth/logout` | Any | Revokes refresh token |

### Books

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/books` | ADMIN | Create book + N copies, optionally assign categories |
| GET | `/api/books` | Any | Paginated list with filters |

**GET /api/books query params:** `title`, `author`, `isbn`, `categoryId` (partial/exact match), `page` (default 0), `size` (default 10), `sort` (default `title`), `dir` (`asc`/`desc`)

### Categories

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/categories` | Any | List all categories |
| POST | `/api/categories` | ADMIN | Create category |
| PUT | `/api/categories/{id}` | ADMIN | Update category |
| DELETE | `/api/categories/{id}` | ADMIN | Delete category (unlinks from books) |

### Borrow, Return & Renewal

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/borrow` | ADMIN | Borrow a book copy (blocked if user has unpaid fines) |
| POST | `/api/return` | ADMIN | Return a book (calculates fine, notifies reservations) |
| POST | `/api/renew` | Any | Renew loan due date |

**POST /api/borrow body:** `{ "userId": 1, "bookId": 1 }`

**POST /api/return body:** `{ "borrowRecordId": 1 }`

**POST /api/renew body:** `{ "borrowRecordId": 1 }`

Renewal conditions (all must pass):
- Record status is `BORROWING` (not `OVERDUE` or `RETURNED`)
- Not overdue (`today <= dueDate`)
- `renewCount < 2` (configurable via `MAX_RENEWALS`)
- No `PENDING` reservation exists for the same book
- READER can only renew their own records

### Borrow Records

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/borrow-records` | ADMIN | Paginated list with filters |
| GET | `/api/borrow-records/{id}` | ADMIN | Single record by ID |
| GET | `/api/borrow-records/my` | Any | Current user's own history |

**GET /api/borrow-records query params:** `userId`, `bookId`, `status` (`BORROWING`/`OVERDUE`/`RETURNED`), `borrowDateFrom`, `borrowDateTo` (`yyyy-MM-dd`), `page`, `size`, `sort` (default `borrowDate`), `dir` (default `desc`)

### Reservations

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/reservations` | Any | Reserve a book (joins queue) |
| GET | `/api/reservations/my` | Any | View own reservations |
| DELETE | `/api/reservations/{id}` | Any | Cancel own reservation |
| GET | `/api/reservations` | ADMIN | All reservations |

When a copy is returned and a `PENDING` reservation exists, the copy is held (`RESERVED`) for 24 hours. If the reserved user borrows within 24 hours, the reservation is `FULFILLED`; otherwise it expires and the copy returns to `AVAILABLE`.

### Fines

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/fines/my-fines` | Any | Own unpaid fines |
| GET | `/api/fines/my-history` | Any | Own fine history (all statuses) |
| GET | `/api/fines` | ADMIN | All fines, filter by `userId` / `status` |
| POST | `/api/fines/{borrowRecordId}/pay` | ADMIN | Mark fine as paid |
| POST | `/api/fines/{borrowRecordId}/waive` | ADMIN | Waive fine |

Fine statuses: `UNPAID` → `PAID` or `WAIVED`. Users with `UNPAID` fines cannot borrow new books.

### Users

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/users` | ADMIN | Paginated user list |
| POST | `/api/users` | ADMIN | Create user with any role |
| PATCH | `/api/users/{id}/deactivate` | ADMIN | Deactivate account |

### Dashboard

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/stats` | ADMIN | Total books, copies, overdue count, user count, top 5 books this month, pending/collected fine totals |

---

## Business Logic Flows

### 1. Authentication
```
POST /register → save User (BCrypt, role=READER)
POST /login    → verify credentials → accessToken (15m) + refreshToken (7d, persisted)
POST /refresh  → validate DB token → rotate (delete old, create new) → return pair
POST /logout   → delete refreshToken from DB
```

### 2. Borrow (`POST /api/borrow`)
```
1. Find User → 404 if not found
2. User has UNPAID Fine? → 402
3. User has NOTIFIED reservation for this book?
   YES → use that RESERVED copy (reservation → FULFILLED)
   NO  → find any AVAILABLE copy → 409 if none
4. copy.status = BORROWED
5. Create BorrowRecord (status=BORROWING, dueDate=today+14d, renewCount=0)
```

### 3. Return (`POST /api/return`)
```
1. Find BorrowRecord → 409 if already RETURNED
2. status = RETURNED, returnDate = today
3. today > dueDate? → fineAmount = days × FINE_PER_DAY (5000 VND)
4. PENDING reservation exists for this book?
   YES → copy.status = RESERVED, reservation → NOTIFIED, expiresAt = now+24h
   NO  → copy.status = AVAILABLE
5. fineAmount > 0? → create Fine entity (status=UNPAID)
```

### 4. Renew (`POST /api/renew`)
```
1. Find BorrowRecord → 404 if READER accessing another user's record
2. status == RETURNED?   → 409
3. today > dueDate?      → 409 (must return + pay fine first)
4. renewCount >= 2?      → 409 (max renewals reached)
5. PENDING reservation for this book? → 409 (someone is waiting)
6. dueDate = today + BORROW_PERIOD_DAYS, renewCount++
```

### 5. Reservation
```
POST /api/reservations → check no duplicate PENDING/NOTIFIED for same book → create (PENDING)

Triggered by Return (step 4):    PENDING → NOTIFIED (copy held 24h)
Triggered by Borrow (step 3):    NOTIFIED → FULFILLED (copy consumed)
Hourly scheduler:                NOTIFIED + expiresAt < now → EXPIRED, copy → AVAILABLE
```

### 6. Fine Scheduler (daily midnight)
```
Find all records where (status=BORROWING OR OVERDUE) AND dueDate < today
→ fineAmount = daysOverdue × FINE_PER_DAY
→ status = OVERDUE
```

### 7. Fine Settlement (Admin)
```
POST /fines/{id}/pay   → UNPAID → PAID,   paidAt = today
POST /fines/{id}/waive → UNPAID → WAIVED
Both throw 409 if fine is not UNPAID
```

### 8. Copy Status Machine
```
AVAILABLE → BORROWED   (borrow)
BORROWED  → AVAILABLE  (return, no pending reservation)
BORROWED  → RESERVED   (return, pending reservation exists)
RESERVED  → BORROWED   (reserved user borrows)
RESERVED  → AVAILABLE  (reservation expires after 24h)
```

### Cross-flow Guards

| Condition | Effect |
|---|---|
| User has `UNPAID` fine | Blocks `POST /borrow` |
| Book has `PENDING` reservation | Blocks `POST /renew` |
| Record is `OVERDUE` | Blocks `POST /renew` |
| User is inactive | Blocks login |

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

`BorrowRecordResponse` fields:

```json
{
  "dueDate": "2026-06-24",
  "overdue": false,
  "fineAmount": 0,
  "renewCount": 1
}
```

Fine = `(overdue days) × 5000 VND`. Persisted by nightly scheduler; also set at return time.

---

## Background Jobs

| Scheduler | Schedule | Description |
|---|---|---|
| `FineScheduler` | Daily at midnight | Sets status `BORROWING` → `OVERDUE` and updates `fineAmount` for all loans past `dueDate` |
| `ReservationScheduler` | Hourly | Expires `NOTIFIED` reservations past 24h; releases held copies back to `AVAILABLE` |

---

## Roles & Permissions

| Action | READER | ADMIN |
|---|---|---|
| Browse books & categories | ✓ | ✓ |
| View own borrow history | ✓ | ✓ |
| Renew own loans | ✓ | ✓ |
| Reserve books | ✓ | ✓ |
| View own fines | ✓ | ✓ |
| Borrow / Return books | — | ✓ |
| Create books & categories | — | ✓ |
| View all borrow records | — | ✓ |
| Manage fines (pay/waive) | — | ✓ |
| View all reservations | — | ✓ |
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

Books: *Clean Code*, *Spring in Action*, *Domain-Driven Design*, *Effective Java*, *Design Patterns* — across 4 categories. Includes sample borrow records, an overdue record with fine, and reservations.

---

## Domain Model

```
User ──< BorrowRecord >── BookCopy >── Book >──< Category
User ──< RefreshToken
User ──< Reservation >── Book
BorrowRecord ──  Fine
```

| Entity | Key fields |
|---|---|
| `User` | username, password (BCrypt), role, active |
| `Book` | title, author, isbn, categories (ManyToMany) |
| `BookCopy` | book, status (`AVAILABLE`/`BORROWED`/`RESERVED`) |
| `BorrowRecord` | user, bookCopy, borrowDate, dueDate, returnDate, status (`BORROWING`/`OVERDUE`/`RETURNED`), fineAmount, renewCount |
| `Fine` | borrowRecord (1:1), amount, status (`UNPAID`/`PAID`/`WAIVED`), createdAt, paidAt |
| `Reservation` | user, book, bookCopy, status (`PENDING`/`NOTIFIED`/`EXPIRED`/`FULFILLED`/`CANCELLED`), expiresAt |
| `Category` | name (unique), description |
| `RefreshToken` | user (1:1), token (UUID), expiresAt |

---

## Project Structure

```
src/main/java/.../
├── config/         # SecurityConfig, JwtUtil, JwtAuthenticationFilter, DataSeeder
├── controller/     # Auth, Book, Borrow, BorrowRecord, Category,
│                   # Fine, Reservation, Stats, User
├── dto/
│   ├── request/    # LoginRequest, CreateBookRequest, BorrowRequest, ...
│   └── response/   # ApiResponse, PageResponse, BookResponse, FineResponse, ...
├── entity/         # User, Book, BookCopy, BorrowRecord, Fine,
│                   # Reservation, Category, RefreshToken
├── exception/      # GlobalExceptionHandler + custom exceptions
├── repository/     # JPA repos + specifications (Book, BorrowRecord, Fine)
├── scheduler/      # FineScheduler, ReservationScheduler
└── service/        # Interfaces + impl/
```
