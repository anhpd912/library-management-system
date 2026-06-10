package fa.training.librarymanagementsystem.dto.request;

import fa.training.librarymanagementsystem.entity.User;
import lombok.Getter;

@Getter
public class CreateUserRequest {
    private String username;
    private String password;
    private User.Role role;
}
