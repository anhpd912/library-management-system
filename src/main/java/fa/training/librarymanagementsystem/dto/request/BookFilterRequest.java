package fa.training.librarymanagementsystem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Query parameters for filtering the book list. All fields are optional. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookFilterRequest {
    private String title;
    private String author;
    private String isbn;
}
