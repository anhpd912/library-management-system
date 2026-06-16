package fa.training.librarymanagementsystem.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateBookRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;
    @NotBlank(message = "Author cannot be blank")
    private String author;
    @NotBlank(message = "ISBN cannot be blank")
    private String isbn;
    private long price;
    @Min(value = 1, message = "Number of copies must be at least 1")
    private int numberOfCopies;
    @NotNull(message = "Category IDs cannot be null")
    @NotEmpty(message = "Category IDs cannot be empty")
    private List<Long> categoryIds;
}