package fa.training.librarymanagementsystem.repository;

import fa.training.librarymanagementsystem.entity.BorrowRecord;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long>, JpaSpecificationExecutor<BorrowRecord> {

    /**
     * Fetches a borrow record with user, bookCopy, and book eagerly loaded.
     * Required by BorrowRecordResponse.from() which accesses all three associations.
     */
    @Query("SELECT r FROM BorrowRecord r JOIN FETCH r.user JOIN FETCH r.bookCopy c JOIN FETCH c.book WHERE r.id = :id")
    Optional<BorrowRecord> findByIdWithDetails(Long id);

    /**
     * Overrides the default findAll to eagerly load user, bookCopy, and book in one query.
     * Safe with pagination because all associations are @ManyToOne (no collection fetch warning).
     */
    @Override
    @EntityGraph(attributePaths = {"user", "bookCopy", "bookCopy.book"})
    Page<BorrowRecord> findAll(@Nullable Specification<BorrowRecord> spec, Pageable pageable);

    /** Counts records with status OVERDUE (set by FineScheduler). */
    @Query("SELECT COUNT(r) FROM BorrowRecord r WHERE r.status = 'OVERDUE'")
    long countOverdue(@Param("today") LocalDate today);

    /** Used by FineScheduler: finds BORROWING records newly past dueDate + all existing OVERDUE records. */
    @Query("SELECT r FROM BorrowRecord r WHERE (r.status = 'BORROWING' OR r.status = 'OVERDUE') AND r.dueDate < :today")
    List<BorrowRecord> findOverdueBorrowing(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(r.fineAmount), 0) FROM BorrowRecord r WHERE r.status = 'OVERDUE' AND r.fineAmount > 0")
    long sumPendingFines();

    @Query("SELECT COALESCE(SUM(r.fineAmount), 0) FROM BorrowRecord r WHERE r.status = 'RETURNED' AND r.fineAmount > 0")
    long sumCollectedFines();

    /** Returns [bookId, bookTitle, borrowCount] for the top N most borrowed books in the given month/year. */
    @Query("SELECT bc.book.id, bc.book.title, COUNT(r.id) FROM BorrowRecord r JOIN r.bookCopy bc " +
           "WHERE MONTH(r.borrowDate) = :month AND YEAR(r.borrowDate) = :year " +
           "GROUP BY bc.book.id, bc.book.title ORDER BY COUNT(r.id) DESC")
    List<Object[]> findTopBorrowedBooks(@Param("month") int month, @Param("year") int year, Pageable pageable);
}
