package fa.training.librarymanagementsystem.config;

import fa.training.librarymanagementsystem.entity.*;
import fa.training.librarymanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds initial data into the database on application startup.
 * The guard on userRepository.count() makes it safe to re-run without duplicating records.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // --- Users ---
        User reader1 = userRepository.save(
                User.builder().username("admin").password(passwordEncoder.encode("admin123")).role(User.Role.ADMIN).active(true).build());
        User reader2 = userRepository.save(
                User.builder().username("reader1").password(passwordEncoder.encode("reader123")).role(User.Role.READER).active(true).build());
        User reader3 = userRepository.save(
                User.builder().username("reader2").password(passwordEncoder.encode("reader123")).role(User.Role.READER).active(true).build());

        // --- Categories ---
        Category cleanCodeCat   = categoryRepository.save(Category.builder().name("Clean Code & Best Practices").description("Readable, maintainable code principles").build());
        Category frameworkCat   = categoryRepository.save(Category.builder().name("Frameworks & Libraries").description("Spring, Hibernate, and ecosystem tools").build());
        Category architectureCat = categoryRepository.save(Category.builder().name("Software Architecture").description("DDD, patterns, and system design").build());
        categoryRepository.save(Category.builder().name("Database & Persistence").description("SQL, NoSQL, ORM best practices").build());

        // --- Books (ArrayList required — Hibernate must be able to mutate the collection for ManyToMany) ---
        Book cleanCode = bookRepository.save(Book.builder().title("Clean Code").author("Robert C. Martin").isbn("9780132350884")
                .categories(new ArrayList<>(List.of(cleanCodeCat, architectureCat))).build());
        Book springInAction = bookRepository.save(Book.builder().title("Spring in Action").author("Craig Walls").isbn("9781617294945")
                .categories(new ArrayList<>(List.of(frameworkCat))).build());
        Book ddd = bookRepository.save(Book.builder().title("Domain-Driven Design").author("Eric Evans").isbn("9780321125217")
                .categories(new ArrayList<>(List.of(architectureCat))).build());
        Book effectiveJava = bookRepository.save(Book.builder().title("Effective Java").author("Joshua Bloch").isbn("9780134685991")
                .categories(new ArrayList<>(List.of(cleanCodeCat))).build());
        Book designPatterns = bookRepository.save(Book.builder().title("Design Patterns").author("Gang of Four").isbn("9780201633610")
                .categories(new ArrayList<>(List.of(architectureCat, cleanCodeCat))).build());

        // --- Copies (save to named variables — used directly below, no findAll needed) ---
        BookCopy cleanCopyBorrowed1 = bookCopyRepository.save(BookCopy.builder().book(cleanCode).status(BookCopy.CopyStatus.BORROWED).build());
        BookCopy cleanCopyBorrowed2 = bookCopyRepository.save(BookCopy.builder().book(cleanCode).status(BookCopy.CopyStatus.BORROWED).build());
        bookCopyRepository.save(BookCopy.builder().book(springInAction).status(BookCopy.CopyStatus.AVAILABLE).build());
        bookCopyRepository.save(BookCopy.builder().book(springInAction).status(BookCopy.CopyStatus.AVAILABLE).build());
        BookCopy dddCopy = bookCopyRepository.save(BookCopy.builder().book(ddd).status(BookCopy.CopyStatus.AVAILABLE).build());
        bookCopyRepository.save(BookCopy.builder().book(effectiveJava).status(BookCopy.CopyStatus.AVAILABLE).build());
        bookCopyRepository.save(BookCopy.builder().book(effectiveJava).status(BookCopy.CopyStatus.AVAILABLE).build());
        BookCopy dpCopyReserved = bookCopyRepository.save(BookCopy.builder().book(designPatterns).status(BookCopy.CopyStatus.RESERVED).build());
        bookCopyRepository.save(BookCopy.builder().book(designPatterns).status(BookCopy.CopyStatus.AVAILABLE).build());

        // --- Borrow records ---
        // reader1 borrowing Clean Code — on time
        borrowRecordRepository.save(BorrowRecord.builder()
                .user(reader2).bookCopy(cleanCopyBorrowed1)
                .borrowDate(LocalDate.now().minusDays(3))
                .dueDate(LocalDate.now().plusDays(11))
                .status(BorrowRecord.BorrowStatus.BORROWING)
                .build());

        // reader2 returned Clean Code — on time, no fine
        borrowRecordRepository.save(BorrowRecord.builder()
                .user(reader3).bookCopy(cleanCopyBorrowed2)
                .borrowDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(6))
                .returnDate(LocalDate.now().minusDays(7))
                .status(BorrowRecord.BorrowStatus.RETURNED)
                .fineAmount(0L)
                .build());
        // mark that copy available since it's been returned
        cleanCopyBorrowed2.setStatus(BookCopy.CopyStatus.AVAILABLE);
        bookCopyRepository.save(cleanCopyBorrowed2);

        // reader2 overdue — 5 days past due, 25000 VND fine persisted
        BorrowRecord overdueRecord = borrowRecordRepository.save(BorrowRecord.builder()
                .user(reader3).bookCopy(cleanCopyBorrowed1)
                .borrowDate(LocalDate.now().minusDays(19))
                .dueDate(LocalDate.now().minusDays(5))
                .status(BorrowRecord.BorrowStatus.OVERDUE)
                .fineAmount(25000L)
                .build());

        // --- Fines ---
        fineRepository.save(Fine.builder()
                .borrowRecord(overdueRecord)
                .amount(25000L)
                .status(Fine.FineStatus.UNPAID)
                .createdAt(LocalDate.now())
                .build());

        // --- Reservations ---
        // reader1 PENDING reservation for DDD
        reservationRepository.save(Reservation.builder()
                .user(reader2).book(ddd)
                .status(Reservation.ReservationStatus.PENDING)
                .reservedAt(LocalDateTime.now().minusHours(2))
                .build());

        // reader2 NOTIFIED reservation for Design Patterns — copy held, 20h left
        reservationRepository.save(Reservation.builder()
                .user(reader3).book(designPatterns).bookCopy(dpCopyReserved)
                .status(Reservation.ReservationStatus.NOTIFIED)
                .reservedAt(LocalDateTime.now().minusHours(4))
                .notifiedAt(LocalDateTime.now().minusHours(4))
                .expiresAt(LocalDateTime.now().plusHours(20))
                .build());
    }
}
