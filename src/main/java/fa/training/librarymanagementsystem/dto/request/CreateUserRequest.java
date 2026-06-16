package fa.training.librarymanagementsystem.dto.request;

import fa.training.librarymanagementsystem.entity.User.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateUserRequest {
    @NotBlank(message = "Username cannot be blank")
    private String username;
    @NotBlank(message = "Password cannot be blank")
    private String password;
    @NotNull(message = "Role cannot be null")
    private Role role;
}