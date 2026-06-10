package fa.training.librarymanagementsystem.dto.response;

import fa.training.librarymanagementsystem.entity.BorrowRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class BorrowRecordResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long bookCopyId;
    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BorrowRecord.BorrowStatus status;
    private boolean overdue;
    /** Fine in VND: 5000/day overdue. 0 if returned on time or still within period. */
    private long fineAmount;

    private static final long FINE_PER_DAY = 5000L;

    public static BorrowRecordResponse from(BorrowRecord record) {
        LocalDate due = record.getDueDate();
        boolean overdue = false;
        long fine = 0;

        if (due != null) {
            if (record.getStatus() == BorrowRecord.BorrowStatus.BORROWING && LocalDate.now().isAfter(due)) {
                overdue = true;
                fine = ChronoUnit.DAYS.between(due, LocalDate.now()) * FINE_PER_DAY;
            } else if (record.getStatus() == BorrowRecord.BorrowStatus.RETURNED
                    && record.getReturnDate() != null && record.getReturnDate().isAfter(due)) {
                fine = ChronoUnit.DAYS.between(due, record.getReturnDate()) * FINE_PER_DAY;
            }
        }

        return BorrowRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUser().getId())
                .username(record.getUser().getUsername())
                .bookCopyId(record.getBookCopy().getId())
                .bookTitle(record.getBookCopy().getBook().getTitle())
                .borrowDate(record.getBorrowDate())
                .dueDate(due)
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .overdue(overdue)
                .fineAmount(fine)
                .build();
    }
}
