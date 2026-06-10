package fa.training.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

/** Represents a system user. Role determines which endpoints the user can access. */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    /** Stored as a BCrypt hash — never plaintext. */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    enum Role {
        ADMIN, READER
    }
}
