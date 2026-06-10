package fa.training.librarymanagementsystem.config;

import fa.training.librarymanagementsystem.entity.*;
import fa.training.librarymanagementsystem.repository.BookCopyRepository;
import fa.training.librarymanagementsystem.repository.BookRepository;
import fa.training.librarymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds initial data into the database on application startup.
 * The guard on userRepository.count() makes it safe to re-run without duplicating records.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        userRepository.saveAll(List.of(
                User.builder().username("admin").password(passwordEncoder.encode("admin123")).role(User.Role.ADMIN).active(true).build(),
                User.builder().username("reader1").password(passwordEncoder.encode("reader123")).role(User.Role.READER).active(true).build(),
                User.builder().username("reader2").password(passwordEncoder.encode("reader123")).role(User.Role.READER).active(true).build()
        ));

        Book cleanCode = bookRepository.save(
                Book.builder().title("Clean Code").author("Robert C. Martin").isbn("9780132350884").build());
        Book springInAction = bookRepository.save(
                Book.builder().title("Spring in Action").author("Craig Walls").isbn("9781617294945").build());
        Book ddd = bookRepository.save(
                Book.builder().title("Domain-Driven Design").author("Eric Evans").isbn("9780321125217").build());

        bookCopyRepository.saveAll(List.of(
                BookCopy.builder().book(cleanCode).status(BookCopy.CopyStatus.AVAILABLE).build(),
                BookCopy.builder().book(cleanCode).status(BookCopy.CopyStatus.AVAILABLE).build(),
                BookCopy.builder().book(springInAction).status(BookCopy.CopyStatus.AVAILABLE).build(),
                BookCopy.builder().book(springInAction).status(BookCopy.CopyStatus.AVAILABLE).build(),
                BookCopy.builder().book(ddd).status(BookCopy.CopyStatus.AVAILABLE).build()
        ));
    }
}
