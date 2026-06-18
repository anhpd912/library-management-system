package fa.training.librarymanagementsystem.dto.request;

import fa.training.librarymanagementsystem.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotNull(message = "Role cannot be null")
    private User.Role role;
}