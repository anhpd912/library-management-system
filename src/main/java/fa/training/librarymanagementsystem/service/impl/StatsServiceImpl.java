package fa.training.librarymanagementsystem.service.impl;

import fa.training.librarymanagementsystem.dto.response.DashboardStatsResponse;
import fa.training.librarymanagementsystem.entity.BookCopy;
import fa.training.librarymanagementsystem.repository.BookCopyRepository;
import fa.training.librarymanagementsystem.repository.BookRepository;
import fa.training.librarymanagementsystem.repository.BorrowRecordRepository;
import fa.training.librarymanagementsystem.repository.UserRepository;
import fa.training.librarymanagementsystem.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .totalBooks(bookRepository.count())
                .totalCopies(bookCopyRepository.count())
                .availableCopies(bookCopyRepository.countByStatus(BookCopy.CopyStatus.AVAILABLE))
                .borrowedCopies(bookCopyRepository.countByStatus(BookCopy.CopyStatus.BORROWED))
                .overdueRecords(borrowRecordRepository.countOverdue(LocalDate.now()))
                .totalUsers(userRepository.count())
                .build();
    }
}
