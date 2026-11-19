package assignment3.core.exam.exceptions;

/**
 * Thrown at the Service Tier when business evaluation, grading, or submission workflows fail.
 * Typically chains a lower-tier cause (e.g. SubmissionStorageException).
 */
public class EvaluationServiceException extends ExamException {
    private final String studentId;
    private final String subjectCode;

    public EvaluationServiceException(String message, String studentId, String subjectCode, Throwable cause) {
        super("ERR_SERVICE_EVALUATION", message, cause);
        this.studentId = studentId;
        this.subjectCode = subjectCode;
    }

    public EvaluationServiceException(String message, String studentId, String subjectCode) {
        super("ERR_SERVICE_EVALUATION", message);
        this.studentId = studentId;
        this.subjectCode = subjectCode;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }
}
