package fa.training.librarymanagementsystem.controller;

import fa.training.librarymanagementsystem.dto.response.ApiResponse;
import fa.training.librarymanagementsystem.dto.request.BookFilterRequest;
import fa.training.librarymanagementsystem.dto.response.BookResponse;
import fa.training.librarymanagementsystem.dto.request.CreateBookRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Manages book catalog operations. POST /api/books requires ADMIN role (enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody CreateBookRequest request) { // Added @Valid here
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created", bookService.createBook(request)));
    }

    /**
     * GET /api/books?title=&author=&isbn=&page=0&size=10&sort=title&dir=asc
     * All parameters are optional and have default values.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookResponse>>> getAllBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        BookFilterRequest filter = new BookFilterRequest(title, author, isbn, categoryId);
        Sort.Direction direction = Sort.Direction.fromOptionalString(dir).orElse(Sort.Direction.ASC);
        return ResponseEntity.ok(ApiResponse.success(
                bookService.getAllBooks(filter, PageRequest.of(page, size, Sort.by(direction, sort)))));
    }
}