package fa.training.librarymanagementsystem.dto.response;

import fa.training.librarymanagementsystem.entity.Fine;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FineResponse {
    private Long id;
    private Long borrowRecordId;
    private Long userId;
    private String username;
    private String bookTitle;
    private long amount;
    private Fine.FineStatus status;
    private Fine.FineReason reason;
    private LocalDate createdAt;
    private LocalDate paidAt;

    public static FineResponse from(Fine fine) {
        return FineResponse.builder()
                .id(fine.getId())
                .borrowRecordId(fine.getBorrowRecord().getId())
                .userId(fine.getBorrowRecord().getUser().getId())
                .username(fine.getBorrowRecord().getUser().getUsername())
                .bookTitle(fine.getBorrowRecord().getBookCopy().getBook().getTitle())
                .amount(fine.getAmount())
                .status(fine.getStatus())
                .reason(fine.getReason())
                .createdAt(fine.getCreatedAt())
                .paidAt(fine.getPaidAt())
                .build();
    }
}
