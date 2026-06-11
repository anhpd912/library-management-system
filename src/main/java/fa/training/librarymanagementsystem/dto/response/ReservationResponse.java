package fa.training.librarymanagementsystem.dto.response;

import fa.training.librarymanagementsystem.entity.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReservationResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private Reservation.ReservationStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime notifiedAt;
    private LocalDateTime expiresAt;

    public static ReservationResponse from(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .username(r.getUser().getUsername())
                .bookId(r.getBook().getId())
                .bookTitle(r.getBook().getTitle())
                .status(r.getStatus())
                .reservedAt(r.getReservedAt())
                .notifiedAt(r.getNotifiedAt())
                .expiresAt(r.getExpiresAt())
                .build();
    }
}
