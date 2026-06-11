package fa.training.librarymanagementsystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "Category name cannot be blank")
    private String name;
    private String description;
}