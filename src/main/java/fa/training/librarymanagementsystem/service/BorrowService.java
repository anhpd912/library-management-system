package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.BorrowRecordFilterRequest;
import fa.training.librarymanagementsystem.dto.BorrowRecordResponse;
import fa.training.librarymanagementsystem.dto.BorrowRequest;
import fa.training.librarymanagementsystem.dto.PageResponse;
import fa.training.librarymanagementsystem.dto.ReturnRequest;
import org.springframework.data.domain.Pageable;

public interface BorrowService {
    BorrowRecordResponse borrow(BorrowRequest request);
    BorrowRecordResponse returnBook(ReturnRequest request);
    PageResponse<BorrowRecordResponse> getAllBorrowRecords(BorrowRecordFilterRequest filter, Pageable pageable);
    BorrowRecordResponse getBorrowRecordById(Long id);
}
