package fa.training.librarymanagementsystem.dto.response;

import fa.training.librarymanagementsystem.entity.BorrowRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

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
    /** Real-time overdue flag — true if still BORROWING past dueDate. */
    private boolean overdue;
    /** Persisted fine in VND: updated daily by FineScheduler; set at return time for RETURNED records. */
    private long fineAmount;

    public static BorrowRecordResponse from(BorrowRecord record) {
        LocalDate due = record.getDueDate();
        boolean overdue = due != null
                && record.getStatus() == BorrowRecord.BorrowStatus.BORROWING
                && LocalDate.now().isAfter(due);

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
                .fineAmount(record.getFineAmount())
                .build();
    }
}
