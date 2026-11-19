package assignment3.core.exam;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Domain entity encapsulating an online exam submission.
 */
public class ExamSubmission {
    private final String submissionId;
    private final String studentId;
    private final String studentName;
    private final String subjectCode;
    private final Map<Integer, String> answers;
    private final LocalDateTime submittedAt;

    public ExamSubmission(String submissionId, String studentId, String studentName, 
                          String subjectCode, Map<Integer, String> answers) {
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.subjectCode = subjectCode;
        this.answers = answers != null ? Collections.unmodifiableMap(answers) : Collections.emptyMap();
        this.submittedAt = LocalDateTime.now();
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
