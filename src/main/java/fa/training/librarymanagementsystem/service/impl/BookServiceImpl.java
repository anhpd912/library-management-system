package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.dto.BookFilterRequest;
import fa.training.librarymanagementsystem.dto.BookResponse;
import fa.training.librarymanagementsystem.dto.CreateBookRequest;
import fa.training.librarymanagementsystem.dto.PageResponse;
import fa.training.librarymanagementsystem.entity.Book;
import fa.training.librarymanagementsystem.entity.BookCopy;
import fa.training.librarymanagementsystem.entity.CopyStatus;
import fa.training.librarymanagementsystem.repository.BookCopyRepository;
import fa.training.librarymanagementsystem.repository.BookRepository;
import fa.training.librarymanagementsystem.repository.BookSpecification;
import fa.training.librarymanagementsystem.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Manages book catalog operations including copy generation. */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    /**
     * Creates a book and generates the requested number of physical copies.
     * Both the Book and BookCopy records are saved in a single transaction.
     */
    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .build();
        book = bookRepository.save(book);

        List<BookCopy> copies = new ArrayList<>();
        for (int i = 0; i < request.getNumberOfCopies(); i++) {
            copies.add(BookCopy.builder()
                    .book(book)
                    .status(CopyStatus.AVAILABLE)
                    .build());
        }
        // Add saved copies to the in-memory collection so BookResponse.from() counts them correctly
        book.getCopies().addAll(bookCopyRepository.saveAll(copies));

        return BookResponse.from(book);
    }

    /**
     * Returns a filtered, paginated list of books.
     * Available copy count is resolved via a COUNT query per book to avoid
     * the HHH90003004 warning caused by JOIN FETCH with pagination.
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> getAllBooks(BookFilterRequest filter, Pageable pageable) {
        Specification<Book> spec = Specification
                .where(BookSpecification.hasTitle(filter.getTitle()))
                .and(BookSpecification.hasAuthor(filter.getAuthor()))
                .and(BookSpecification.hasIsbn(filter.getIsbn()));

        Page<Book> page = bookRepository.findAll(spec, pageable);

        List<BookResponse> content = page.getContent().stream()
                .map(book -> BookResponse.from(book,
                        bookCopyRepository.countByBookIdAndStatus(book.getId(), CopyStatus.AVAILABLE)))
                .toList();

        return PageResponse.from(page, content);
    }
}
