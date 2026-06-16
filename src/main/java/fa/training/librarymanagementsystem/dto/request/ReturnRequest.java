package fa.training.librarymanagementsystem.dto.request;

import lombok.Data;

@Data
public class ReturnRequest {
    private Long borrowRecordId;
    /** True if the copy is reported lost instead of physically returned. */
    private Boolean lost;
    /** Optional admin-assessed damage fee in VND, added on top of any late fine. */
    private Long damageFee;
}
