package fa.training.librarymanagementsystem.repository;

import fa.training.librarymanagementsystem.entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long>, JpaSpecificationExecutor<Fine> {

    Optional<Fine> findByBorrowRecordId(Long borrowRecordId);

    boolean existsByBorrowRecordUserIdAndStatus(Long userId, Fine.FineStatus status);

    /** Eager-loads all associations needed by FineResponse.from(). */
    @Query("SELECT f FROM Fine f JOIN FETCH f.borrowRecord r JOIN FETCH r.user JOIN FETCH r.bookCopy c JOIN FETCH c.book WHERE r.user.username = :username AND f.status = :status")
    List<Fine> findByUsernameAndStatus(@Param("username") String username, @Param("status") Fine.FineStatus status);

    @Query("SELECT f FROM Fine f JOIN FETCH f.borrowRecord r JOIN FETCH r.user JOIN FETCH r.bookCopy c JOIN FETCH c.book WHERE r.user.username = :username")
    Page<Fine> findByUsername(@Param("username") String username, Pageable pageable);
}
