package fa.training.librarymanagementsystem.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardStatsResponse {
    private long totalBooks;
    private long totalCopies;
    private long availableCopies;
    private long borrowedCopies;
    private long overdueRecords;
    private long totalUsers;
    private long totalPendingFines;
    private long totalCollectedFines;
    private List<BookBorrowStatResponse> topBorrowedBooksThisMonth;
}
