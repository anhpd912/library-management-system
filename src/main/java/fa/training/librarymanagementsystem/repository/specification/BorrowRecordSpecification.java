package fa.training.librarymanagementsystem.repository.specification;

import fa.training.librarymanagementsystem.entity.BorrowRecord;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * JPA Specification factories for dynamic BorrowRecord filtering.
 * Returning null from a predicate is treated as no-op by Specification.where().
 */
public class BorrowRecordSpecification {

    private BorrowRecordSpecification() {}

    public static Specification<BorrowRecord> hasUserId(Long userId) {
        return (root, query, cb) ->
                userId == null ? null :
                cb.equal(root.get("user").get("id"), userId);
    }

    /** Traverses bookCopy -> book to filter by book id. */
    public static Specification<BorrowRecord> hasBookId(Long bookId) {
        return (root, query, cb) ->
                bookId == null ? null :
                cb.equal(root.get("bookCopy").get("book").get("id"), bookId);
    }

    public static Specification<BorrowRecord> hasStatus(BorrowStatus status) {
        return (root, query, cb) ->
                status == null ? null :
                cb.equal(root.get("status"), status);
    }

    public static Specification<BorrowRecord> borrowedFrom(LocalDate date) {
        return (root, query, cb) ->
                date == null ? null :
                cb.greaterThanOrEqualTo(root.get("borrowDate"), date);
    }

    public static Specification<BorrowRecord> borrowedTo(LocalDate date) {
        return (root, query, cb) ->
                date == null ? null :
                cb.lessThanOrEqualTo(root.get("borrowDate"), date);
    }
}
