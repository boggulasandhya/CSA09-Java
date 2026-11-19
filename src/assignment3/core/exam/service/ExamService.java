package assignment3.core.exam.service;

import assignment3.core.exam.ExamSubmission;
import assignment3.core.exam.exceptions.EvaluationServiceException;
import assignment3.core.exam.exceptions.ExamException;
import assignment3.core.exam.exceptions.ExamValidationException;
import assignment3.core.exam.exceptions.SubmissionStorageException;
import assignment3.core.exam.storage.ExamStorage;
import assignment3.core.util.ConsoleUtil;

/**
 * Service Tier: Enforces business validation rules, scoring workflows,
 * and coordinates between Controller and Storage layers.
 */
public class ExamService {

    private final ExamStorage storage;

    public ExamService(ExamStorage storage) {
        this.storage = storage;
    }

    /**
     * Validates and processes the submission.
     *
     * @param submission The exam submission entity
     * @throws ExamValidationException If submission violates academic rules
     * @throws EvaluationServiceException If service processing or storage fails (chained)
     */
    public void processSubmission(ExamSubmission submission) 
            throws ExamException {
        
        System.out.println("  [Service Tier] Validating submission: " + submission.getSubmissionId() 
                + " for Student: " + submission.getStudentId());

        // Business Validation Rule 1: Student ID must not be null/blank
        if (submission.getStudentId() == null || submission.getStudentId().isBlank()) {
            throw new ExamValidationException("studentId", "Student Identification number cannot be empty.");
        }

        // Business Validation Rule 2: Subject code must be provided
        if (submission.getSubjectCode() == null || submission.getSubjectCode().isBlank()) {
            throw new ExamValidationException("subjectCode", "Subject course code is required.");
        }

        // Business Validation Rule 3: Must submit answers
        if (submission.getAnswers() == null || submission.getAnswers().isEmpty()) {
            throw new ExamValidationException("answers", "Submission cannot be empty. At least one answer is required.");
        }

        System.out.println(ConsoleUtil.GREEN + "  [Service Tier] Validation passed. Delegating to Storage Tier..." + ConsoleUtil.RESET);

        // Interact with Storage Tier
        try {
            storage.persist(submission);
            System.out.println(ConsoleUtil.GREEN + "  [Service Tier] Submission processed and cataloged successfully." + ConsoleUtil.RESET);

        } catch (SubmissionStorageException storageEx) {
            // EXCEPTION CHAINING AT SERVICE TIER:
            // Intercept storage-tier exception and chain it into an EvaluationServiceException
            // with high-level student/course context.
            throw new EvaluationServiceException(
                    "Service processing pipeline failed for student " + submission.getStudentId() 
                    + " enrolled in course " + submission.getSubjectCode(),
                    submission.getStudentId(),
                    submission.getSubjectCode(),
                    storageEx // Chained exception (cause)
            );
        }
    }
}
