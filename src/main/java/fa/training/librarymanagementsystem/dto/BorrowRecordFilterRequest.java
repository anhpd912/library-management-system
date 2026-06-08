package fa.training.librarymanagementsystem.dto;

import fa.training.librarymanagementsystem.entity.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Query parameters for filtering the borrow records list. All fields are optional. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordFilterRequest {
    private Long userId;
    private Long bookId;
    private BorrowStatus status;
    private LocalDate borrowDateFrom;
    private LocalDate borrowDateTo;
}
