package fa.training.librarymanagementsystem.repository;

import fa.training.librarymanagementsystem.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /** Loads all books with their copies in one query to avoid N+1 when computing available copy counts. */
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.copies")
    List<Book> findAllWithCopies();
}
