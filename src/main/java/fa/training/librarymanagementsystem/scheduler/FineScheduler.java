package fa.training.librarymanagementsystem.scheduler;

import fa.training.librarymanagementsystem.entity.BorrowRecord;
import fa.training.librarymanagementsystem.repository.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Runs at midnight daily to persist accumulated fine amounts on overdue borrow records. */
@Slf4j
@Component
@RequiredArgsConstructor
public class FineScheduler {

    private final BorrowRecordRepository borrowRecordRepository;

    @Value("${app.borrow.fine-per-day:5000}")
    private long finePerDay;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateOverdueFines() {
        LocalDate today = LocalDate.now();
        List<BorrowRecord> overdue = borrowRecordRepository.findOverdueBorrowing(today);
        if (overdue.isEmpty()) return;

        for (BorrowRecord record : overdue) {
            long days = ChronoUnit.DAYS.between(record.getDueDate(), today);
            record.setFineAmount(days * finePerDay);
        }
        borrowRecordRepository.saveAll(overdue);
        log.info("Fine scheduler: updated {} overdue records (fine/day: {} VND)", overdue.size(), finePerDay);
    }
}
