package fa.training.librarymanagementsystem.exception;

public class RenewalNotAllowedException extends RuntimeException {
    public RenewalNotAllowedException(String message) {
        super(message);
    }
}
