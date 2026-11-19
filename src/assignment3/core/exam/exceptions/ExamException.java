package assignment3.core.exam.exceptions;

import java.time.LocalDateTime;

/**
 * Base checked exception for all Online Examination Portal errors.
 */
public class ExamException extends Exception {
    private final String errorCode;
    private final LocalDateTime timestamp;

    public ExamException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public ExamException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
