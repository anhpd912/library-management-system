# Kịch bản thuyết trình: Library Management System

> Paste từng slide vào PowerPoint, hoặc dùng tool convert Markdown→PPTX (Pandoc: `pandoc presentation-script.md -o demo.pptx`)

---

## Slide 1 — Tiêu đề
**Library Management System**
Hệ thống quản lý thư viện — REST API
Spring Boot 4 + JWT Authentication

---

## Slide 2 — Tech Stack
- **Framework:** Spring Boot 4.0.6 / Java 17
- **Security:** Spring Security + JWT (stateless, access 15p / refresh 7 ngày)
- **Persistence:** Spring Data JPA + MySQL
- **API Docs:** SpringDoc OpenAPI (Swagger UI)
- **Khác:** Lombok, Docker Compose

---

## Slide 3 — Domain Model
```
User ──< BorrowRecord >── BookCopy >── Book >──< Category
User ──< Reservation >── Book
BorrowRecord ── Fine
```
- `User`: username, role (ADMIN/READER), active
- `Book`: title, author, isbn, **price** (giá trị thay thế)
- `BookCopy`: trạng thái AVAILABLE / BORROWED / RESERVED / **LOST**
- `BorrowRecord`: trạng thái BORROWING / OVERDUE / RETURNED
- `Fine`: UNPAID/PAID/WAIVED, reason LATE/DAMAGE/LOST

---

## Slide 4 — Tính năng chính
1. Mượn / Trả sách
2. **Gia hạn** (renew) — tối đa 2 lần, không quá hạn
3. **Đặt trước** (reservation) — giữ sách 24h khi có người trả
4. **Quản lý phạt** — trễ hạn, hư hỏng, mất sách
5. Quản lý Category (CRUD, gắn với Book)
6. Dashboard thống kê

---

## Slide 5 — Luồng Mượn sách
```
1. Kiểm tra User có phạt UNPAID? → chặn (402)
2. Có reservation đang giữ cho user? → dùng copy đó
3. Không → tìm copy AVAILABLE
4. Tạo BorrowRecord, dueDate = hôm nay + 14 ngày
```

---

## Slide 6 — Luồng Trả sách (mở rộng: hư hỏng / mất sách)
```
- Trả bình thường, trễ hạn → tính fine = số ngày trễ × 5,000đ
- Trả + báo hư hỏng (damageFee) → cộng thêm vào fine
- Trả + báo mất sách (lost=true) → copy chuyển LOST,
  fine = giá sách (book.price)
- Có người đang đặt trước? → giữ copy 24h cho họ
```

---

## Slide 7 — Luồng Gia hạn
```
Điều kiện (phải đủ cả 5):
✓ Đang BORROWING (chưa RETURNED)
✓ Chưa quá hạn (today <= dueDate)
✓ renewCount < 2
✓ Không có ai đặt trước sách này
✓ Reader chỉ gia hạn record của chính mình
→ dueDate += 14 ngày, renewCount++
```

---

## Slide 8 — Luồng Đặt trước (Reservation)
```
PENDING ──(có copy trả về)──> NOTIFIED (giữ 24h)
   │                              │
   └─(huỷ)──> CANCELLED      (mượn trong 24h)──> FULFILLED
                                   │
                            (quá 24h)──> EXPIRED
```

---

## Slide 9 — Dashboard & Background Jobs
- **Dashboard:** tổng số sách, số đang mượn, top 5 sách mượn nhiều nhất tháng, tổng phạt chờ thu / đã thu
- **FineScheduler** (0h hàng ngày): BORROWING quá hạn → OVERDUE, tính phạt
- **ReservationScheduler** (mỗi giờ): hết hạn NOTIFIED 24h → EXPIRED, copy về AVAILABLE

---

## Slide 10 — Demo Live
1. Login (admin/reader) qua Swagger
2. Tạo sách + category
3. Mượn → Gia hạn (2 lần, lần 3 lỗi) → Trả (tính phạt)
4. Báo hư hỏng / mất sách khi trả
5. Đặt trước → trả sách khác → thấy reservation NOTIFIED
6. Xem Dashboard

*(Chi tiết từng bước: xem `docs/demo-guide.md`)*

---

## Slide 11 — Kết luận
- Hệ thống đầy đủ nghiệp vụ thư viện thực tế: mượn/trả/gia hạn/đặt trước/phạt
- Kiến trúc rõ ràng: Controller → Service → Repository, JWT stateless
- Mở rộng tốt: thêm category, fine reason, lost/damage không phá vỡ flow cũ

---

## Phụ lục — Dummy Data dùng khi demo

### Tài khoản
| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |
| reader1 | reader123 | READER |
| reader2 | reader123 | READER |

### Sách mẫu (đã seed sẵn)
| Sách | Số bản | Category |
|---|---|---|
| Clean Code | 2 | Clean Code & Best Practices |
| Spring in Action | 2 | Frameworks & Libraries |
| Domain-Driven Design | 1 | Software Architecture |
| Effective Java | 2 | Clean Code & Best Practices |
| Design Patterns | 2 | Software Architecture |

