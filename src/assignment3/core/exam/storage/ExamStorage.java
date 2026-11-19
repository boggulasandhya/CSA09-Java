package assignment3.core.exam.storage;

import assignment3.core.exam.ExamSubmission;
import assignment3.core.exam.exceptions.SubmissionStorageException;
import assignment3.core.util.ConsoleUtil;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage Tier: Simulates database and file-system persistence for exam submissions.
 * Translates low-level system I/O errors into SubmissionStorageException.
 */
public class ExamStorage {

    private final ConcurrentHashMap<String, ExamSubmission> database = new ConcurrentHashMap<>();
    private boolean simulateDiskWriteFailure = false;

    public void setSimulateDiskWriteFailure(boolean simulateDiskWriteFailure) {
        this.simulateDiskWriteFailure = simulateDiskWriteFailure;
    }

    /**
     * Persists the submission to disk/database storage.
     *
     * @param submission The exam submission to save
     * @throws SubmissionStorageException If physical write or database commit fails
     */
    public void persist(ExamSubmission submission) throws SubmissionStorageException {
        System.out.println("   [Storage Tier] Attempting to persist submission: " + submission.getSubmissionId()
                + " to persistent volume '/var/data/exams/" + submission.getSubjectCode() + "/'");

        try {
            if (simulateDiskWriteFailure) {
                // Simulate underlying operating system / storage hardware I/O crash
                throw new IOException("POSIX write() failed: Disk full or write permission denied on cluster node.");
            }

            // Normal successful persistence
            database.put(submission.getSubmissionId(), submission);
            System.out.println(ConsoleUtil.GREEN + "   [Storage Tier] Submission " + submission.getSubmissionId() 
                    + " committed successfully to database." + ConsoleUtil.RESET);

        } catch (IOException ioException) {
            // EXCEPTION CHAINING AT STORAGE TIER:
            // Wrap low-level IOException inside domain SubmissionStorageException
            throw new SubmissionStorageException(
                    "Database / Disk persistence failure while storing submission for ID: " + submission.getSubmissionId(),
                    "/var/data/exams/" + submission.getSubjectCode() + "/" + submission.getSubmissionId() + ".dat",
                    ioException
            );
        }
    }

    public ExamSubmission findById(String submissionId) {
        return database.get(submissionId);
    }
}
