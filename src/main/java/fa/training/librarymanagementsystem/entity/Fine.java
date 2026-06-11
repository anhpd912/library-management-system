package fa.training.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Represents a finalized fine charge created when an overdue book is returned.
 * One Fine per BorrowRecord. Only created when BorrowRecord.fineAmount > 0 at return time.
 */
@Entity
@Table(name = "fines")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_record_id", nullable = false, unique = true)
    private BorrowRecord borrowRecord;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private FineStatus status;

    @Column(nullable = false)
    private LocalDate createdAt;

    /** Set when admin marks the fine as PAID. */
    private LocalDate paidAt;

    public enum FineStatus {
        UNPAID, PAID, WAIVED
    }
}
