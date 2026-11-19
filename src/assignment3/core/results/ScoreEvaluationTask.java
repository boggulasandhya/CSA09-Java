package assignment3.core.results;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Callable task evaluating a single student score record/file.
 * Guarantees fault isolation: any parsing or data corruption exception
 * is caught and encapsulated inside a ProcessingResult without crashing the thread pool.
 */
public class ScoreEvaluationTask implements Callable<ProcessingResult> {

    private final String fileId;
    private final String rawRecordPayload;

    public ScoreEvaluationTask(String fileId, String rawRecordPayload) {
        this.fileId = fileId;
        this.rawRecordPayload = rawRecordPayload;
    }

    @Override
    public ProcessingResult call() {
        long startNanos = System.nanoTime();

        try {
            // Fault check 1: null or empty data
            if (rawRecordPayload == null || rawRecordPayload.isBlank()) {
                return ProcessingResult.failure(fileId, ProcessingResult.ErrorCategory.MISSING_MANDATORY_FIELD,
                        "Record payload is empty or null.", elapsedMicros(startNanos));
            }

            // Expected format: RollNo|StudentName|Subject1:Mark1,Subject2:Mark2,...
            String[] sections = rawRecordPayload.split("\\|");
            if (sections.length < 3) {
                return ProcessingResult.failure(fileId, ProcessingResult.ErrorCategory.CORRUPTED_FORMAT,
                        "Malformed record structure; expected 3 piped sections, got " + sections.length,
                        elapsedMicros(startNanos));
            }

            String rollNo = sections[0].trim();
            String name = sections[1].trim();
            String marksChunk = sections[2].trim();

            if (rollNo.isEmpty() || name.isEmpty()) {
                return ProcessingResult.failure(fileId, ProcessingResult.ErrorCategory.MISSING_MANDATORY_FIELD,
                        "Student roll number or name is empty.", elapsedMicros(startNanos));
            }

            // Parse subject marks
            Map<String, Double> subjectMarks = new HashMap<>();
            String[] subjectEntries = marksChunk.split(",");
            for (String entry : subjectEntries) {
                String[] pair = entry.split(":");
                if (pair.length != 2) {
                    return ProcessingResult.failure(fileId, ProcessingResult.ErrorCategory.CORRUPTED_FORMAT,
                            "Invalid subject mark pair: " + entry, elapsedMicros(startNanos));
                }

                String subject = pair[0].trim();
                double mark;
                try {
                    mark = Double.parseDouble(pair[1].trim());
                } catch (NumberFormatException nfe) {
                    return ProcessingResult.failure(fileId, ProcessingResult.ErrorCategory.CORRUPTED_FORMAT,
                            "Non-numeric score value '" + pair[1] + "' for subject " + subject,
                            elapsedMicros(startNanos));
                }

                // Range validation (0 - 100)
                if (mark < 0.0 || mark > 100.0) {
                    return ProcessingResult.failure(fileId, ProcessingResult.ErrorCategory.INVALID_SCORE_RANGE,
                            "Score " + mark + " for " + subject + " is out of bounds (allowed: 0.0 - 100.0)",
                            elapsedMicros(startNanos));
                }

                subjectMarks.put(subject, mark);
            }

            StudentScore score = new StudentScore(rollNo, name, subjectMarks);
            return ProcessingResult.success(fileId, score, elapsedMicros(startNanos));

        } catch (Exception unexpectedEx) {
            // General fault isolation boundary
            return ProcessingResult.failure(fileId, ProcessingResult.ErrorCategory.CORRUPTED_FORMAT,
                    "Unexpected processing exception: " + unexpectedEx.getMessage(),
                    elapsedMicros(startNanos));
        }
    }

    private static long elapsedMicros(long startNanos) {
        return (System.nanoTime() - startNanos) / 1000;
    }
}
