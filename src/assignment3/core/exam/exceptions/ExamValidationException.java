package assignment3.core.exam.exceptions;

/**
 * Thrown when an exam submission violates validation constraints
 * (e.g., blank answers, missing student identity, submission deadline passed).
 */
public class ExamValidationException extends ExamException {
    private final String fieldName;

    public ExamValidationException(String fieldName, String message) {
        super("ERR_VALIDATION", message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
