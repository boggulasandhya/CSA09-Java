package assignment3.core.results;

/**
 * Encapsulates the execution outcome of an individual score file evaluation.
 */
public class ProcessingResult {

    public enum Status {
        SUCCESS,
        FAILURE
    }

    public enum ErrorCategory {
        NONE,
        CORRUPTED_FORMAT,
        INVALID_SCORE_RANGE,
        MISSING_MANDATORY_FIELD,
        IO_DISK_READ_ERROR
    }

    private final String fileIdentifier;
    private final Status status;
    private final StudentScore studentScore;
    private final ErrorCategory errorCategory;
    private final String errorMessage;
    private final long durationMicros;

    private ProcessingResult(String fileIdentifier, Status status, StudentScore studentScore,
                             ErrorCategory errorCategory, String errorMessage, long durationMicros) {
        this.fileIdentifier = fileIdentifier;
        this.status = status;
        this.studentScore = studentScore;
        this.errorCategory = errorCategory;
        this.errorMessage = errorMessage;
        this.durationMicros = durationMicros;
    }

    public static ProcessingResult success(String fileId, StudentScore score, long durationMicros) {
        return new ProcessingResult(fileId, Status.SUCCESS, score, ErrorCategory.NONE, null, durationMicros);
    }

    public static ProcessingResult failure(String fileId, ErrorCategory category, String message, long durationMicros) {
        return new ProcessingResult(fileId, Status.FAILURE, null, category, message, durationMicros);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public Status getStatus() {
        return status;
    }

    public StudentScore getStudentScore() {
        return studentScore;
    }

    public ErrorCategory getErrorCategory() {
        return errorCategory;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getDurationMicros() {
        return durationMicros;
    }
}
