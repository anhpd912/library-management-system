package fa.training.librarymanagementsystem.repository;

import fa.training.librarymanagementsystem.entity.BookCopy;
import fa.training.librarymanagementsystem.entity.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    Optional<BookCopy> findFirstByBookIdAndStatus(Long bookId, CopyStatus status);

    /** Used to compute availableCopies per book without loading the full copies collection. */
    long countByBookIdAndStatus(Long bookId, CopyStatus status);
}
