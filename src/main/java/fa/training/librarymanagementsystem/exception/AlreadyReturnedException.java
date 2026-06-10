package fa.training.librarymanagementsystem.exception;

public class AlreadyReturnedException extends RuntimeException {
    public AlreadyReturnedException(String message) {
        super(message);
    }
}
