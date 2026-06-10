package fa.training.librarymanagementsystem.controller;

import fa.training.librarymanagementsystem.dto.response.ApiResponse;
import fa.training.librarymanagementsystem.dto.request.BorrowRecordFilterRequest;
import fa.training.librarymanagementsystem.dto.response.BorrowRecordResponse;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.entity.BorrowRecord;
import fa.training.librarymanagementsystem.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** Admin-only endpoints for viewing and querying borrow history. */
@RestController
@RequestMapping("/api/borrow-records")
@RequiredArgsConstructor
public class BorrowRecordController {

    private final BorrowService borrowService;

    /**
     * GET /api/borrow-records?userId=&bookId=&status=&borrowDateFrom=&borrowDateTo=
     *                        &page=0&size=10&sort=borrowDate&dir=desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BorrowRecordResponse>>> getAllBorrowRecords(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) BorrowRecord.BorrowStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate borrowDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate borrowDateTo,
            @RequestParam(defaultValue = "0")            int page,
            @RequestParam(defaultValue = "10")           int size,
            @RequestParam(defaultValue = "borrowDate")   String sort,
            @RequestParam(defaultValue = "desc")         String dir) {

        BorrowRecordFilterRequest filter =
                new BorrowRecordFilterRequest(userId, bookId, status, borrowDateFrom, borrowDateTo);
        Sort.Direction direction = Sort.Direction.fromOptionalString(dir).orElse(Sort.Direction.DESC);

        return ResponseEntity.ok(ApiResponse.success(
                borrowService.getAllBorrowRecords(filter, PageRequest.of(page, size, Sort.by(direction, sort)))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> getBorrowRecordById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(borrowService.getBorrowRecordById(id)));
    }

    /**
     * GET /api/borrow-records/my — reader's own borrow history (any authenticated user).
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<BorrowRecordResponse>>> getMyBorrowRecords(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "10")         int size,
            @RequestParam(defaultValue = "borrowDate") String sort,
            @RequestParam(defaultValue = "desc")       String dir,
            Authentication authentication) {

        Sort.Direction direction = Sort.Direction.fromOptionalString(dir).orElse(Sort.Direction.DESC);
        return ResponseEntity.ok(ApiResponse.success(
                borrowService.getMyBorrowRecords(
                        authentication.getName(),
                        PageRequest.of(page, size, Sort.by(direction, sort)))));
    }
}
