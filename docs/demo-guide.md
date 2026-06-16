# Demo Guide

## 1. Setup
```bash
docker-compose up -d        # MySQL + app together, OR:
./mvnw spring-boot:run       # app only, needs MySQL running
```
Wait for app start (port 8080). Seed data auto-loads:
- `admin` / `admin123` (ADMIN)
- `reader1` / `reader123` (READER)
- `reader2` / `reader123` (READER)

## 2. Open Swagger
`http://localhost:8080/swagger-ui.html`

## 3. Login flow
- `POST /api/auth/login` with `admin/admin123` → copy `accessToken`
- Click **Authorize** (top-right) → paste token
- Mention `POST /api/auth/refresh` and `POST /api/auth/logout` also exist

## 4. Books & Categories
- `GET /api/categories` → 4 seeded categories
- `GET /api/books?categoryId=1` → filter demo
- `POST /api/books` → create book with `categoryIds` (ADMIN only)

## 5. Borrow flow
- `GET /api/borrow-records/my` (as reader1) → existing loan
- `POST /api/borrow` (as admin) `{userId, bookId}` → new loan, dueDate +14d
- Note: `BookCopy.status` flips AVAILABLE → BORROWED

## 6. Renew flow (highlight feature)
- `POST /api/renew` `{borrowRecordId}` as reader1 on own record → success, `renewCount=1`
- Call again → `renewCount=2`
- Call 3rd time → 409 max renewals reached
- Try renewing the seeded OVERDUE record → 409 must return first

## 7. Return + Fine
- `POST /api/return` `{borrowRecordId: <overdue one>}` → `fineAmount` computed, `Fine.reason=LATE`
- `GET /api/fines/my-history` (as reader2) → see UNPAID fine
- `GET /api/fines` (as admin) → see all fines, `reason` field shown
- **Before paying**, try `POST /api/borrow` as reader2 → 402 blocked (unpaid fine)
- `POST /api/fines/{borrowRecordId}/pay` → mark PAID

## 7b. Damage / Lost book (new feature)
- `POST /api/return` `{borrowRecordId, damageFee: 20000}` → fine = lateFine + 20000, `reason=DAMAGE`
- `POST /api/return` `{borrowRecordId, lost: true}` → copy → `LOST` status (out of circulation), fine = `book.price`, `reason=LOST`
- Show `POST /api/books` now accepts `price` field — set it before demoing lost-book fine

## 8. Reservation flow
- `POST /api/reservations` `{bookId}` as reader2 on a fully-borrowed book → PENDING
- `POST /api/return` that book's copy (as admin) → reservation auto-becomes NOTIFIED, copy → RESERVED
- `GET /api/reservations/my` as reader2 → NOTIFIED status + `expiresAt` (24h)
- `POST /api/borrow` as reader2 for that book → consumes reserved copy, FULFILLED

## 9. Dashboard
- `GET /api/stats` as admin → totalBooks, overdue count, top 5 books this month, pending/collected fine totals

## 10. Wrap-up talking points
- Scheduled jobs: `FineScheduler` (midnight, BORROWING→OVERDUE), `ReservationScheduler` (hourly, expires NOTIFIED)
- Role-based access: READER vs ADMIN throughout
- Full API + business flow docs in `README.md`

---

**Order matters for #6 and #7** — demo overdue/fine before paying it off, so the unpaid-fine-blocks-borrow rule is visible. Dry-run once to confirm seed data IDs match expectations before the live demo.
