package fa.training.librarymanagementsystem.controller;

import fa.training.librarymanagementsystem.dto.ApiResponse;
import fa.training.librarymanagementsystem.dto.BorrowRecordResponse;
import fa.training.librarymanagementsystem.dto.BorrowRequest;
import fa.training.librarymanagementsystem.dto.ReturnRequest;
import fa.training.librarymanagementsystem.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Handles borrow and return transactions. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/borrow")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> borrow(@RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book borrowed successfully", borrowService.borrow(request)));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> returnBook(@RequestBody ReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Book returned successfully", borrowService.returnBook(request)));
    }
}
