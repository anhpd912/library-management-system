package fa.training.librarymanagementsystem.repository;

import fa.training.librarymanagementsystem.entity.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    Optional<BookCopy> findFirstByBookIdAndStatus(Long bookId, BookCopy.CopyStatus status);

    /** Used to compute availableCopies per book without loading the full copies collection. */
    long countByBookIdAndStatus(Long bookId, BookCopy.CopyStatus status);

    long countByStatus(BookCopy.CopyStatus status);
}
