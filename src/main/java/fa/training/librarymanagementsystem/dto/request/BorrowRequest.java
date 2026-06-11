package fa.training.librarymanagementsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;
}