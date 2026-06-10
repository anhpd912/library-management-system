package fa.training.librarymanagementsystem.service;

import fa.training.librarymanagementsystem.dto.request.CreateUserRequest;
import fa.training.librarymanagementsystem.dto.response.PageResponse;
import fa.training.librarymanagementsystem.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    PageResponse<UserResponse> getAllUsers(Pageable pageable);
    UserResponse createUser(CreateUserRequest request);
    UserResponse deactivateUser(Long id);
}
