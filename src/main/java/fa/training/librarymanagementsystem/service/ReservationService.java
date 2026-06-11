package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.request.CreateReservationRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.dto.response.ReservationResponse;
import fa.training.librarymanagementsystem.entity.BookCopy;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request, String username);
    ReservationResponse cancelReservation(Long id, String username);
    PageResponse<ReservationResponse> getMyReservations(String username, Pageable pageable);
    PageResponse<ReservationResponse> getAllReservations(Pageable pageable);

    /** Called by BorrowServiceImpl.returnBook — notifies the oldest PENDING reservation.
     *  Sets the copy to RESERVED and returns true if a reservation was notified. */
    boolean notifyReservation(Long bookId, BookCopy copy);

    /** Called by BorrowServiceImpl.borrow — fulfills a NOTIFIED reservation for the user.
     *  Returns the held BookCopy if the user has a valid NOTIFIED reservation, empty otherwise. */
    Optional<BookCopy> fulfillReservation(Long bookId, Long userId);

    /** Called hourly by ReservationScheduler — sets NOTIFIED reservations past expiresAt to EXPIRED. */
    void expireOverdue();
}
