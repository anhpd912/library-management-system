package fa.training.librarymanagementsystem.scheduler;

import fa.training.librarymanagementsystem.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Runs every hour to expire NOTIFIED reservations whose 24h window has passed. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    @Scheduled(cron = "0 0 * * * *")
    public void expireOverdueReservations() {
        log.debug("Reservation expiry check running");
        reservationService.expireOverdue();
    }
}
