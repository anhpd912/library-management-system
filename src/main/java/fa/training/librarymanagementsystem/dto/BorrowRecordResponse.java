package fa.training.librarymanagementsystem.dto;

import fa.training.librarymanagementsystem.entity.BorrowRecord;
import fa.training.librarymanagementsystem.entity.BorrowStatus;
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
    private LocalDate returnDate;
    private BorrowStatus status;

    public static BorrowRecordResponse from(BorrowRecord record) {
        return BorrowRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUser().getId())
                .username(record.getUser().getUsername())
                .bookCopyId(record.getBookCopy().getId())
                .bookTitle(record.getBookCopy().getBook().getTitle())
                .borrowDate(record.getBorrowDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .build();
    }
}
