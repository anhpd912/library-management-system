package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.response.FineResponse;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.entity.BorrowRecord;
import fa.training.librarymanagementsystem.entity.Fine;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FineService {

    /** Creates a Fine(UNPAID) linked to the given record. Called only when record.fineAmount > 0. */
    void createFine(BorrowRecord record, Fine.FineReason reason);

    /** Returns true if the user has any UNPAID fines. Used to block new borrows. */
    boolean hasUnpaidFines(Long userId);

    List<FineResponse> getMyUnpaidFines(String username);

    PageResponse<FineResponse> getMyFineHistory(String username, Pageable pageable);

    /** Admin: all fines, optionally filtered by userId and/or status. */
    PageResponse<FineResponse> getAllFines(Long userId, Fine.FineStatus status, Pageable pageable);

    FineResponse markAsPaid(Long borrowRecordId);

    FineResponse waive(Long borrowRecordId);

    void deleteFine(Long id);
}