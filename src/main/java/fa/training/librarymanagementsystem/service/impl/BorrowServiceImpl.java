package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.dto.request.BorrowRecordFilterRequest;
import fa.training.librarymanagementsystem.dto.response.BorrowRecordResponse;
import fa.training.librarymanagementsystem.dto.request.BorrowRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.dto.request.ReturnRequest;
import fa.training.librarymanagementsystem.entity.*;
import fa.training.librarymanagementsystem.exception.AlreadyReturnedException;
import fa.training.librarymanagementsystem.exception.BookNotAvailableException;
import fa.training.librarymanagementsystem.exception.ResourceNotFoundException;
import fa.training.librarymanagementsystem.repository.BookCopyRepository;
import fa.training.librarymanagementsystem.repository.BorrowRecordRepository;
import fa.training.librarymanagementsystem.repository.specification.BorrowRecordSpecification;
import fa.training.librarymanagementsystem.repository.UserRepository;
import fa.training.librarymanagementsystem.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Core transaction logic for borrowing, returning, and querying borrow records. */
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final UserRepository userRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    @Value("${app.borrow.period-days:14}")
    private int borrowPeriodDays;

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

        // Pick any available copy; the specific copy does not matter to the borrower
        BookCopy copy = bookCopyRepository.findFirstByBookIdAndStatus(request.getBookId(), BookCopy.CopyStatus.AVAILABLE)
                .orElseThrow(() -> new BookNotAvailableException("No available copy for book id: " + request.getBookId()));

        copy.setStatus(BookCopy.CopyStatus.BORROWED);

        LocalDate today = LocalDate.now();
        BorrowRecord record = BorrowRecord.builder()
                .user(user)
                .bookCopy(copy)
                .borrowDate(today)
                .dueDate(today.plusDays(borrowPeriodDays))
                .returnDate(null)
                .status(BorrowRecord.BorrowStatus.BORROWING)
                .build();

        return BorrowRecordResponse.from(borrowRecordRepository.save(record));
    }

    /**
     * Processes the return of a borrowed book.
     * JPA dirty checking propagates the AVAILABLE status on bookCopy without an explicit save call.
     */
    @Override
    @Transactional
    public BorrowRecordResponse returnBook(ReturnRequest request) {
        // Use JOIN FETCH query to avoid LazyInitializationException when mapping to DTO
        BorrowRecord record = borrowRecordRepository.findByIdWithDetails(request.getBorrowRecordId())
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord not found: " + request.getBorrowRecordId()));

        if (record.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            throw new AlreadyReturnedException("BorrowRecord " + request.getBorrowRecordId() + " already returned");
        }

        record.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        record.setReturnDate(LocalDate.now());
        record.getBookCopy().setStatus(BookCopy.CopyStatus.AVAILABLE);

        return BorrowRecordResponse.from(borrowRecordRepository.save(record));
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
}
