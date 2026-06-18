# JSON mẫu copy-paste cho demo

## Auth
```json
// POST /api/auth/register
{ "username": "reader3", "password": "reader123" }

// POST /api/auth/login
{ "username": "admin", "password": "admin123" }

// POST /api/auth/refresh
{ "refreshToken": "<paste refresh token>" }
```

## Books
```json
// POST /api/books
{ "title": "The Pragmatic Programmer", "author": "David Thomas", "isbn": "9780135957059",
  "price": 250000, "numberOfCopies": 2, "categoryIds": [1] }

// PUT /api/books/{id}
{ "title": "Clean Code (2nd Edition)", "author": "Robert C. Martin", "isbn": "9780132350884",
  "price": 190000, "numberOfCopies": 2, "categoryIds": [1, 3] }
```

## Categories
```json
// POST /api/categories
{ "name": "Testing & QA", "description": "Unit testing, TDD, QA practices" }

// PUT /api/categories/{id}
{ "name": "Testing & QA", "description": "Updated description" }
```

## Borrow / Return / Renew
```json
// POST /api/borrow
{ "userId": 2, "bookId": 1 }

// POST /api/return — bình thường
{ "borrowRecordId": 1 }

// POST /api/return — báo hư hỏng
{ "borrowRecordId": 1, "damageFee": 30000 }

// POST /api/return — báo mất sách
{ "borrowRecordId": 1, "lost": true }

// POST /api/renew
{ "borrowRecordId": 1 }
```

## Reservations
```json
// POST /api/reservations
{ "bookId": 3 }
```

## Users
```json
// POST /api/users
{ "username": "newreader", "password": "pass123", "role": "READER" }

// PUT /api/users/{id} (nếu có UpdateUserRequest)
{ "active": false }
```

## Fines
```json
// POST /api/fines/{borrowRecordId}/pay  → không cần body
// POST /api/fines/{borrowRecordId}/waive → không cần body
```

---
Endpoint không cần body (GET, DELETE, fines pay/waive) — chỉ cần path param, để trống request body.
