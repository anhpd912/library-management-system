package fa.training.librarymanagementsystem.controller;

import fa.training.librarymanagementsystem.dto.request.CreateReservationRequest;
import fa.training.librarymanagementsystem.dto.response.ApiResponse;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.dto.response.ReservationResponse;
import fa.training.librarymanagementsystem.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Reservation management.
 * POST / DELETE /my are open to any authenticated user.
 * GET / (all reservations) requires ADMIN (enforced in SecurityConfig).
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @RequestBody CreateReservationRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reservation created",
                        reservationService.createReservation(request, auth.getName())));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getMyReservations(
            Authentication auth,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reservedAt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(dir).orElse(Sort.Direction.DESC);
        return ResponseEntity.ok(ApiResponse.success(
                reservationService.getMyReservations(auth.getName(), PageRequest.of(page, size, Sort.by(direction, sort)))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled",
                reservationService.cancelReservation(id, auth.getName())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> getAllReservations(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reservedAt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(dir).orElse(Sort.Direction.DESC);
        return ResponseEntity.ok(ApiResponse.success(
                reservationService.getAllReservations(PageRequest.of(page, size, Sort.by(direction, sort)))));
    }
}
