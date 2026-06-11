package fa.training.librarymanagementsystem.dto.response;

import fa.training.librarymanagementsystem.entity.Book;
import fa.training.librarymanagementsystem.entity.BookCopy;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private long availableCopies;
    private List<CategoryResponse> categories;

    /** Used after createBook — copies and categories already loaded in-memory. */
    public static BookResponse from(Book book) {
        long available = book.getCopies().stream()
                .filter(c -> c.getStatus() == BookCopy.CopyStatus.AVAILABLE)
                .count();
        return from(book, available);
    }

    /** Used in paginated list — caller provides pre-computed count to avoid N+1 on copies. */
    public static BookResponse from(Book book, long availableCopies) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .availableCopies(availableCopies)
                .categories(book.getCategories().stream().map(CategoryResponse::from).toList())
                .build();
    }
}
