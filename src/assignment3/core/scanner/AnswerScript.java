package assignment3.core.scanner;

/**
 * Domain entity representing an examination answer script booklet to be scanned.
 */
public class AnswerScript {
    private final String scriptId;
    private final String studentRoll;
    private final String subjectCode;
    private final int pageCount;
    private boolean scanned;
    private long scanDurationMs;

    public AnswerScript(String scriptId, String studentRoll, String subjectCode, int pageCount) {
        this.scriptId = scriptId;
        this.studentRoll = studentRoll;
        this.subjectCode = subjectCode;
        this.pageCount = pageCount;
        this.scanned = false;
        this.scanDurationMs = 0;
    }

    public String getScriptId() {
        return scriptId;
    }

    public String getStudentRoll() {
        return studentRoll;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public int getPageCount() {
        return pageCount;
    }

    public boolean isScanned() {
        return scanned;
    }

    public void markScanned(long durationMs) {
        this.scanned = true;
        this.scanDurationMs = durationMs;
    }

    public long getScanDurationMs() {
        return scanDurationMs;
    }

    @Override
    public String toString() {
        return String.format("[%s | Roll: %s | Pages: %d | Status: %s]",
                scriptId, studentRoll, pageCount, scanned ? "SCANNED" : "PENDING");
    }
}
