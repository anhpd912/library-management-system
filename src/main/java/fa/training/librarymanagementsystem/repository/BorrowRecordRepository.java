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

    @Query("SELECT COUNT(r) FROM BorrowRecord r WHERE r.status = 'BORROWING' AND r.dueDate < :today")
    long countOverdue(@Param("today") LocalDate today);
}
