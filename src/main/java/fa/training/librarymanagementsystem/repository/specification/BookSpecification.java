package fa.training.librarymanagementsystem.repository.specification;

import fa.training.librarymanagementsystem.entity.Book;
import fa.training.librarymanagementsystem.entity.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specification factories for dynamic Book filtering.
 * Each method returns null when the parameter is blank, which Specification.where() treats as no-op.
 */
public class BookSpecification {

    private BookSpecification() {}

    public static Specification<Book> hasTitle(String title) {
        return (root, query, cb) ->
                (title == null || title.isBlank()) ? null :
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Book> hasAuthor(String author) {
        return (root, query, cb) ->
                (author == null || author.isBlank()) ? null :
                cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
    }

    public static Specification<Book> hasIsbn(String isbn) {
        return (root, query, cb) ->
                (isbn == null || isbn.isBlank()) ? null :
                cb.equal(root.get("isbn"), isbn);
    }

    public static Specification<Book> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            query.distinct(true);
            Join<Book, Category> categories = root.join("categories", JoinType.INNER);
            return cb.equal(categories.get("id"), categoryId);
        };
    }
}
