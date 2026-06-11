package fa.training.librarymanagementsystem.dto.request;

import lombok.Data;

@Data
public class CreateCategoryRequest {
    private String name;
    private String description;
}
