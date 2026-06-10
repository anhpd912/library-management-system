package fa.training.librarymanagementsystem.repository.specification;

import fa.training.librarymanagementsystem.entity.Book;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specification factories for dynamic Book filtering.
 * Each method returns null when the parameter is blank, which Specification.where() treats as no-op.
 */
public class BookSpecification {

    private BookSpecification() {}

    /** Case-insensitive partial match on title. */
    public static Specification<Book> hasTitle(String title) {
        return (root, query, cb) ->
                (title == null || title.isBlank()) ? null :
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    /** Case-insensitive partial match on author. */
    public static Specification<Book> hasAuthor(String author) {
        return (root, query, cb) ->
                (author == null || author.isBlank()) ? null :
                cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
    }

    /** Exact match on ISBN. */
    public static Specification<Book> hasIsbn(String isbn) {
        return (root, query, cb) ->
                (isbn == null || isbn.isBlank()) ? null :
                cb.equal(root.get("isbn"), isbn);
    }
}
