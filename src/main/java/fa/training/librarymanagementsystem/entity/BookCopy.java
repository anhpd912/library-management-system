package fa.training.librarymanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;

/** Represents a single physical copy of a Book. Status reflects current availability. */
@Entity
@Table(name = "book_copies")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CopyStatus status;
}
