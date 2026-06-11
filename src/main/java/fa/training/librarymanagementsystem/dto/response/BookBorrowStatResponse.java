package fa.training.librarymanagementsystem.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookBorrowStatResponse {
    private Long bookId;
    private String bookTitle;
    private long borrowCount;
}
