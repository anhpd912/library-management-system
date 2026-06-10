package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.request.BookFilterRequest;
import fa.training.librarymanagementsystem.dto.response.BookResponse;
import fa.training.librarymanagementsystem.dto.request.CreateBookRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookResponse createBook(CreateBookRequest request);
    PageResponse<BookResponse> getAllBooks(BookFilterRequest filter, Pageable pageable);
}
