package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.request.BorrowRecordFilterRequest;
import fa.training.librarymanagementsystem.dto.response.BorrowRecordResponse;
import fa.training.librarymanagementsystem.dto.request.BorrowRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.dto.request.ReturnRequest;
import org.springframework.data.domain.Pageable;

public interface BorrowService {
    BorrowRecordResponse borrow(BorrowRequest request);
    BorrowRecordResponse returnBook(ReturnRequest request);
    PageResponse<BorrowRecordResponse> getAllBorrowRecords(BorrowRecordFilterRequest filter, Pageable pageable);
    BorrowRecordResponse getBorrowRecordById(Long id);
    PageResponse<BorrowRecordResponse> getMyBorrowRecords(String username, Pageable pageable);

    /** Extends the due date of an active, non-overdue loan. READER may only renew their own records. */
    BorrowRecordResponse renewBook(Long borrowRecordId, String requesterUsername);
}
