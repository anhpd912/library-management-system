package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.dto.request.BorrowRecordFilterRequest;
import fa.training.librarymanagementsystem.dto.response.BorrowRecordResponse;
import fa.training.librarymanagementsystem.dto.request.BorrowRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.dto.request.ReturnRequest;
import fa.training.librarymanagementsystem.entity.*;
import fa.training.librarymanagementsystem.exception.AlreadyReturnedException;
import fa.training.librarymanagementsystem.exception.BookNotAvailableException;
import fa.training.librarymanagementsystem.exception.RenewalNotAllowedException;
import fa.training.librarymanagementsystem.exception.ResourceNotFoundException;
import fa.training.librarymanagementsystem.exception.UnpaidFineException;
import fa.training.librarymanagementsystem.repository.ReservationRepository;
import fa.training.librarymanagementsystem.repository.BookCopyRepository;
import fa.training.librarymanagementsystem.repository.BorrowRecordRepository;
import fa.training.librarymanagementsystem.repository.specification.BorrowRecordSpecification;
import fa.training.librarymanagementsystem.repository.UserRepository;
import fa.training.librarymanagementsystem.service.BorrowService;
import fa.training.librarymanagementsystem.service.FineService;
import fa.training.librarymanagementsystem.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/** Core transaction logic for borrowing, returning, and querying borrow records. */
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final UserRepository userRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final FineService fineService;

    @Value("${app.borrow.period-days:14}")
    private int borrowPeriodDays;

    @Value("${app.borrow.fine-per-day:5000}")
    private long finePerDay;

    @Value("${app.borrow.max-renewals:2}")
    private int maxRenewals;

    /**
     * Borrows a book copy for a user.
     * The status update and record creation are atomic — if saving the record fails,
     * the copy status is also rolled back.
     */
    @Override
    @Transactional
    public BorrowRecordResponse borrow(BorrowRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        if (fineService.hasUnpaidFines(user.getId())) {
            throw new UnpaidFineException("User " + user.getUsername() + " has unpaid fines. Please settle before borrowing.");
        }

        // Priority: use the RESERVED copy if user has a NOTIFIED reservation; otherwise pick any AVAILABLE copy
        BookCopy copy;
        Optional<BookCopy> reservedCopy = reservationService.fulfillReservation(request.getBookId(), user.getId());
        if (reservedCopy.isPresent()) {
            copy = reservedCopy.get();
        } else {
            copy = bookCopyRepository.findFirstByBookIdAndStatus(request.getBookId(), BookCopy.CopyStatus.AVAILABLE)
                    .orElseThrow(() -> new BookNotAvailableException("No available copy for book id: " + request.getBookId()));
        }

        copy.setStatus(BookCopy.CopyStatus.BORROWED);

        LocalDate today = LocalDate.now();
        BorrowRecord record = BorrowRecord.builder()
                .user(user)
                .bookCopy(copy)
                .borrowDate(today)
                .dueDate(today.plusDays(borrowPeriodDays))
                .status(BorrowRecord.BorrowStatus.BORROWING)
                .build();

        return BorrowRecordResponse.from(borrowRecordRepository.save(record));
    }

    @Override
    @Transactional
    public BorrowRecordResponse returnBook(ReturnRequest request) {
        BorrowRecord record = borrowRecordRepository.findByIdWithDetails(request.getBorrowRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord not found: " + request.getBorrowRecordId()));

        if (record.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new AlreadyReturnedException("BorrowRecord " + request.getBorrowRecordId() + " already returned");
        }

        LocalDate today = LocalDate.now();
        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        record.setReturnDate(today);

        // Persist fine at return time so dashboard sums are accurate
        LocalDate due = record.getDueDate();
        if (due != null && today.isAfter(due)) {
            record.setFineAmount(ChronoUnit.DAYS.between(due, today) * finePerDay);
        }

        // Check for waiting reservation — if found the copy becomes RESERVED, otherwise AVAILABLE
        boolean notified = reservationService.notifyReservation(
                record.getBookCopy().getBook().getId(), record.getBookCopy());
        if (!notified) {
            record.getBookCopy().setStatus(BookCopy.CopyStatus.AVAILABLE);
        }

        BorrowRecord saved = borrowRecordRepository.save(record);

        // Create a Fine entity if overdue so admin can track payment status
        if (saved.getFineAmount() > 0) {
            fineService.createFine(saved);
        }

        return BorrowRecordResponse.from(saved);
    }

    /**
     * Returns a filtered, paginated list of borrow records.
     * EntityGraph on the repository method loads all associations in one query — no N+1.
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<BorrowRecordResponse> getAllBorrowRecords(BorrowRecordFilterRequest filter, Pageable pageable) {
        Specification<BorrowRecord> spec = Specification
                .where(BorrowRecordSpecification.hasUserId(filter.getUserId()))
                .and(BorrowRecordSpecification.hasBookId(filter.getBookId()))
                .and(BorrowRecordSpecification.hasStatus(filter.getStatus()))
                .and(BorrowRecordSpecification.borrowedFrom(filter.getBorrowDateFrom()))
                .and(BorrowRecordSpecification.borrowedTo(filter.getBorrowDateTo()));

        Page<BorrowRecord> page = borrowRecordRepository.findAll(spec, pageable);
        List<BorrowRecordResponse> content = page.getContent().stream()
                .map(BorrowRecordResponse::from)
                .toList();

        return PageResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowRecordResponse getBorrowRecordById(Long id) {
        return BorrowRecordResponse.from(
                borrowRecordRepository.findByIdWithDetails(id)
                        .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord not found: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BorrowRecordResponse> getMyBorrowRecords(String username, Pageable pageable) {
        Specification<BorrowRecord> spec = Specification
                .where(BorrowRecordSpecification.hasUsername(username));
        Page<BorrowRecord> page = borrowRecordRepository.findAll(spec, pageable);
        List<BorrowRecordResponse> content = page.getContent().stream()
                .map(BorrowRecordResponse::from)
                .toList();
        return PageResponse.from(page, content);
    }

    @Override
    @Transactional
    public BorrowRecordResponse renewBook(Long borrowRecordId, String requesterUsername) {
        BorrowRecord record = borrowRecordRepository.findByIdWithDetails(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord not found: " + borrowRecordId));

        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requesterUsername));

        // READER can only renew their own record; use 404 to avoid leaking existence to other readers
        if (requester.getRole() == User.Role.READER
                && !record.getUser().getId().equals(requester.getId())) {
            throw new ResourceNotFoundException("BorrowRecord not found: " + borrowRecordId);
        }

        if (record.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new RenewalNotAllowedException("Cannot renew an already returned record");
        }

        LocalDate today = LocalDate.now();
        if (today.isAfter(record.getDueDate())) {
            throw new RenewalNotAllowedException("Book is overdue — return it and settle any fines before renewing");
        }

        if (record.getRenewCount() >= maxRenewals) {
            throw new RenewalNotAllowedException("Maximum renewals (" + maxRenewals + ") reached for this loan");
        }

        Long bookId = record.getBookCopy().getBook().getId();
        boolean hasPendingReservation = reservationRepository
                .findFirstByBookIdAndStatusOrderByReservedAtAsc(bookId, Reservation.ReservationStatus.PENDING)
                .isPresent();
        if (hasPendingReservation) {
            throw new RenewalNotAllowedException("Cannot renew — another user is waiting for this book");
        }

        record.setDueDate(today.plusDays(borrowPeriodDays));
        record.setRenewCount(record.getRenewCount() + 1);

        return BorrowRecordResponse.from(borrowRecordRepository.save(record));
    }
}
