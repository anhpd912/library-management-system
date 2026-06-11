package fa.training.librarymanagementsystem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnRequest {
    @NotNull(message = "Borrow record ID cannot be null")
    private Long borrowRecordId;
}