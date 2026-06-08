package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.BookFilterRequest;
import fa.training.librarymanagementsystem.dto.BookResponse;
import fa.training.librarymanagementsystem.dto.CreateBookRequest;
import fa.training.librarymanagementsystem.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookResponse createBook(CreateBookRequest request);
    PageResponse<BookResponse> getAllBooks(BookFilterRequest filter, Pageable pageable);
}
