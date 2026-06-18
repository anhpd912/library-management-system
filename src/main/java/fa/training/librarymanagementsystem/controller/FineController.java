package fa.training.librarymanagementsystem.controller;

import fa.training.librarymanagementsystem.dto.response.ApiResponse;
import fa.training.librarymanagementsystem.dto.response.FineResponse;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.entity.Fine;
import fa.training.librarymanagementsystem.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    /** Reader: list own UNPAID fines. */
    @GetMapping("/my-fines")
    public ResponseEntity<ApiResponse<List<FineResponse>>> getMyUnpaidFines(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(fineService.getMyUnpaidFines(principal.getName())));
    }

    /** Reader: paginated history of all own fines regardless of status. */
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<PageResponse<FineResponse>>> getMyFineHistory(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(dir).orElse(Sort.Direction.DESC);
        PageResponse<FineResponse> result = fineService.getMyFineHistory(
                principal.getName(), PageRequest.of(page, size, Sort.by(direction, sort)));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** Admin: all fines, filterable by userId and/or status. */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FineResponse>>> getAllFines(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Fine.FineStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(dir).orElse(Sort.Direction.DESC);
        PageResponse<FineResponse> result = fineService.getAllFines(
                userId, status, PageRequest.of(page, size, Sort.by(direction, sort)));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** Admin: mark a fine as paid. */
    @PostMapping("/{borrowRecordId}/pay")
    public ResponseEntity<ApiResponse<FineResponse>> markAsPaid(@PathVariable Long borrowRecordId) {
        return ResponseEntity.ok(ApiResponse.success("Fine marked as paid", fineService.markAsPaid(borrowRecordId)));
    }

    /** Admin: waive a fine (forgive without payment). */
    @PostMapping("/{borrowRecordId}/waive")
    public ResponseEntity<ApiResponse<FineResponse>> waive(@PathVariable Long borrowRecordId) {
        return ResponseEntity.ok(ApiResponse.success("Fine waived", fineService.waive(borrowRecordId)));
    }

    /** Admin: delete a fine record. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFine(@PathVariable Long id) {
        fineService.deleteFine(id);
        return ResponseEntity.ok(ApiResponse.success("Fine deleted", null));
    }
}