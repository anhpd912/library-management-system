package fa.training.librarymanagementsystem.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {
    private long totalBooks;
    private long totalCopies;
    private long availableCopies;
    private long borrowedCopies;
    private long overdueRecords;
    private long totalUsers;
}
