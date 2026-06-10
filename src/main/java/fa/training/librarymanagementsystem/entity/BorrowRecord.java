package fa.training.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Records a single borrow transaction.
 * returnDate is null while the copy is still borrowed (status = BORROWING).
 */
@Entity
@Table(name = "borrow_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(nullable = false)
    private LocalDate borrowDate;

    /** Due date = borrowDate + configured period (default 14 days). */
    private LocalDate dueDate;

    /**
     * Null until the book is returned.
     */
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status;

    public enum BorrowStatus {
        BORROWING, RETURNED
    }

}
