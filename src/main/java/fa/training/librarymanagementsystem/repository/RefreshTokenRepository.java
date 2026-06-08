package fa.training.librarymanagementsystem.repository;

import fa.training.librarymanagementsystem.entity.RefreshToken;
import fa.training.librarymanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    /** Removes the existing token before issuing a new one on login or rotation. */
    void deleteByUser(User user);
}
