package fa.training.librarymanagementsystem.repository;

import fa.training.librarymanagementsystem.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** Oldest PENDING reservation for a book — first in queue. */
    Optional<Reservation> findFirstByBookIdAndStatusOrderByReservedAtAsc(Long bookId, Reservation.ReservationStatus status);

    /** NOTIFIED reservations whose 24h window has passed — candidates for expiry. */
    List<Reservation> findByStatusAndExpiresAtBefore(Reservation.ReservationStatus status, LocalDateTime now);

    Page<Reservation> findByUserUsername(String username, Pageable pageable);

    Optional<Reservation> findByBookIdAndUserIdAndStatus(Long bookId, Long userId, Reservation.ReservationStatus status);

    boolean existsByBookIdAndUserIdAndStatusIn(Long bookId, Long userId, List<Reservation.ReservationStatus> statuses);
}
