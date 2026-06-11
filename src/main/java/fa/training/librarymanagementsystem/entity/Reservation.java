package fa.training.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks a user's reservation for a book title.
 * When a copy is returned and a PENDING reservation exists, the copy is held (RESERVED)
 * for 24 hours while the reservation moves to NOTIFIED.
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    /** The specific copy held for this reservation; set when status moves to NOTIFIED. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id")
    private BookCopy bookCopy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime reservedAt = LocalDateTime.now();

    /** Set when a copy becomes available and the reservation is notified. */
    private LocalDateTime notifiedAt;

    /** reservedAt + 24h; after this the copy is released back to AVAILABLE. */
    private LocalDateTime expiresAt;

    public enum ReservationStatus {
        PENDING, NOTIFIED, EXPIRED, FULFILLED, CANCELLED
    }
}
