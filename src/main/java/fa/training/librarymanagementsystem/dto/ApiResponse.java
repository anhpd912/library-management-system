package fa.training.librarymanagementsystem.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Generic wrapper for all API responses.
 * Provides a consistent JSON shape: { success, message, data }.
 */
@Getter
@Builder
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    /** Returns a successful response with data and a custom message. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /** Returns a successful response with data and a default "Success" message. */
    public static <T> ApiResponse<T> success(T data) {
        return success("Success", data);
    }

    /** Returns a failed response with no data. */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}
