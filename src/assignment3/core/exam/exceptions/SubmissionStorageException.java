package assignment3.core.exam.exceptions;

/**
 * Thrown at the Storage Tier when exam submission persistence fails
 * (e.g., database connection timeout, disk quota exceeded, I/O corruption).
 */
public class SubmissionStorageException extends ExamException {
    private final String targetStorageLocation;

    public SubmissionStorageException(String message, String targetStorageLocation, Throwable cause) {
        super("ERR_STORAGE_FAIL", message, cause);
        this.targetStorageLocation = targetStorageLocation;
    }

    public SubmissionStorageException(String message, String targetStorageLocation) {
        super("ERR_STORAGE_FAIL", message);
        this.targetStorageLocation = targetStorageLocation;
    }

    public String getTargetStorageLocation() {
        return targetStorageLocation;
    }
}
