package fa.training.librarymanagementsystem.dto.request;

import lombok.Data;

@Data
public class BorrowRequest {
    private Long userId;
    private Long bookId;
}
