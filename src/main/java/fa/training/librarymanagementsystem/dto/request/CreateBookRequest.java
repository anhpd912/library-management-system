package fa.training.librarymanagementsystem.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateBookRequest {
    private String title;
    private String author;
    private String isbn;
    private int numberOfCopies;
    private List<Long> categoryIds;
}
