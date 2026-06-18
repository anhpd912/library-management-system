package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.dto.request.BookFilterRequest;
import fa.training.librarymanagementsystem.dto.response.BookResponse;
import fa.training.librarymanagementsystem.dto.request.CreateBookRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.entity.Book;
import fa.training.librarymanagementsystem.entity.BookCopy;
import fa.training.librarymanagementsystem.entity.Category;
import fa.training.librarymanagementsystem.exception.ResourceNotFoundException;
import fa.training.librarymanagementsystem.repository.BookCopyRepository;
import fa.training.librarymanagementsystem.repository.BookRepository;
import fa.training.librarymanagementsystem.repository.CategoryRepository;
import fa.training.librarymanagementsystem.repository.specification.BookSpecification;
import fa.training.librarymanagementsystem.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Manages book catalog operations including copy generation. */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Creates a book and generates the requested number of physical copies.
     * Both the Book and BookCopy records are saved in a single transaction.
     */
    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) {
        List<Category> categories = request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()
                ? categoryRepository.findAllById(request.getCategoryIds())
                : Collections.emptyList();

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .price(request.getPrice())
                .categories(new ArrayList<>(categories))
                .build();
        book = bookRepository.save(book);

        List<BookCopy> copies = new ArrayList<>();
        for (int i = 0; i < request.getNumberOfCopies(); i++) {
            copies.add(BookCopy.builder()
                    .book(book)
                    .status(BookCopy.CopyStatus.AVAILABLE)
                    .build());
        }
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
        Specification<Book> isActive = (root, query, cb) -> cb.isTrue(root.get("active"));
        Specification<Book> spec = Specification
                .where(isActive)
                .and(BookSpecification.hasTitle(filter.getTitle()))
                .and(BookSpecification.hasAuthor(filter.getAuthor()))
                .and(BookSpecification.hasIsbn(filter.getIsbn()))
                .and(BookSpecification.hasCategory(filter.getCategoryId()));

        Page<Book> page = bookRepository.findAll(spec, pageable);

        List<BookResponse> content = page.getContent().stream()
                .map(book -> BookResponse.from(book,
                        bookCopyRepository.countByBookIdAndStatus(book.getId(), BookCopy.CopyStatus.AVAILABLE)))
                .toList();

        return PageResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        long availableCopies = bookCopyRepository.countByBookIdAndStatus(id, BookCopy.CopyStatus.AVAILABLE);
        return BookResponse.from(book, availableCopies);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, CreateBookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        List<Category> categories = request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()
                ? categoryRepository.findAllById(request.getCategoryIds())
                : Collections.emptyList();

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategories(new ArrayList<>(categories));

        book = bookRepository.save(book);
        return BookResponse.from(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        book.setActive(false);
        bookRepository.save(book);
    }
}