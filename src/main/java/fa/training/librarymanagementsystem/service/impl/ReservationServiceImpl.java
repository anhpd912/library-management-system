package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.dto.request.CreateReservationRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.dto.response.ReservationResponse;
import fa.training.librarymanagementsystem.entity.Book;
import fa.training.librarymanagementsystem.entity.BookCopy;
import fa.training.librarymanagementsystem.entity.Reservation;
import fa.training.librarymanagementsystem.exception.ResourceAlreadyExistsException;
import fa.training.librarymanagementsystem.exception.ResourceNotFoundException;
import fa.training.librarymanagementsystem.repository.BookCopyRepository;
import fa.training.librarymanagementsystem.repository.BookRepository;
import fa.training.librarymanagementsystem.repository.ReservationRepository;
import fa.training.librarymanagementsystem.repository.UserRepository;
import fa.training.librarymanagementsystem.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + request.getBookId()));

        boolean alreadyActive = reservationRepository.existsByBookIdAndUserIdAndStatusIn(
                book.getId(), user.getId(),
                List.of(Reservation.ReservationStatus.PENDING, Reservation.ReservationStatus.NOTIFIED));
        if (alreadyActive) {
            throw new ResourceAlreadyExistsException("Active reservation already exists for this book");
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .status(Reservation.ReservationStatus.PENDING)
                .build();
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long id, String username) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));

        if (!reservation.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Reservation not found: " + id);
        }
        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING
                && reservation.getStatus() != Reservation.ReservationStatus.NOTIFIED) {
            throw new IllegalStateException("Cannot cancel a reservation with status: " + reservation.getStatus());
        }

        // If NOTIFIED, release the held copy back to AVAILABLE
        if (reservation.getStatus() == Reservation.ReservationStatus.NOTIFIED
                && reservation.getBookCopy() != null) {
            reservation.getBookCopy().setStatus(BookCopy.CopyStatus.AVAILABLE);
            bookCopyRepository.save(reservation.getBookCopy());
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getMyReservations(String username, Pageable pageable) {
        Page<Reservation> page = reservationRepository.findByUserUsername(username, pageable);
        return PageResponse.from(page, page.getContent().stream().map(ReservationResponse::from).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReservationResponse> getAllReservations(Pageable pageable) {
        Page<Reservation> page = reservationRepository.findAll(pageable);
        return PageResponse.from(page, page.getContent().stream().map(ReservationResponse::from).toList());
    }

    @Override
    @Transactional
    public boolean notifyReservation(Long bookId, BookCopy copy) {
        Optional<Reservation> opt = reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAsc(bookId, Reservation.ReservationStatus.PENDING);
        if (opt.isEmpty()) return false;

        Reservation reservation = opt.get();
        LocalDateTime now = LocalDateTime.now();
        copy.setStatus(BookCopy.CopyStatus.RESERVED);
        bookCopyRepository.save(copy);

        reservation.setStatus(Reservation.ReservationStatus.NOTIFIED);
        reservation.setBookCopy(copy);
        reservation.setNotifiedAt(now);
        reservation.setExpiresAt(now.plusHours(24));
        reservationRepository.save(reservation);

        log.info("Reservation {} notified for book {} — copy {} held until {}",
                reservation.getId(), bookId, copy.getId(), reservation.getExpiresAt());
        return true;
    }

    @Override
    @Transactional
    public Optional<BookCopy> fulfillReservation(Long bookId, Long userId) {
        Optional<Reservation> opt = reservationRepository
                .findByBookIdAndUserIdAndStatus(bookId, userId, Reservation.ReservationStatus.NOTIFIED);
        if (opt.isEmpty()) return Optional.empty();

        Reservation reservation = opt.get();
        reservation.setStatus(Reservation.ReservationStatus.FULFILLED);
        reservationRepository.save(reservation);
        return Optional.of(reservation.getBookCopy());
    }

    @Override
    @Transactional
    public void expireOverdue() {
        List<Reservation> expired = reservationRepository
                .findByStatusAndExpiresAtBefore(Reservation.ReservationStatus.NOTIFIED, LocalDateTime.now());
        if (expired.isEmpty()) return;

        for (Reservation r : expired) {
            r.setStatus(Reservation.ReservationStatus.EXPIRED);
            if (r.getBookCopy() != null) {
                r.getBookCopy().setStatus(BookCopy.CopyStatus.AVAILABLE);
                bookCopyRepository.save(r.getBookCopy());
            }
        }
        reservationRepository.saveAll(expired);
        log.info("Expired {} overdue reservations", expired.size());
    }
}
