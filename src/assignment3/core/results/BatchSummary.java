package assignment3.core.results;

import assignment3.core.util.ConsoleUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aggregates batch evaluation statistics and renders structured summaries.
 */
public class BatchSummary {

    private final AtomicInteger totalEvaluated = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    private final Map<ProcessingResult.ErrorCategory, AtomicInteger> errorCounts = new EnumMap<>(ProcessingResult.ErrorCategory.class);
    private final Map<String, AtomicInteger> gradeDistribution = new ConcurrentHashMap<>();

    private long startTimestampMillis;
    private long endTimestampMillis;

    public BatchSummary() {
        for (ProcessingResult.ErrorCategory cat : ProcessingResult.ErrorCategory.values()) {
            if (cat != ProcessingResult.ErrorCategory.NONE) {
                errorCounts.put(cat, new AtomicInteger(0));
            }
        }
        gradeDistribution.put("A", new AtomicInteger(0));
        gradeDistribution.put("B", new AtomicInteger(0));
        gradeDistribution.put("C", new AtomicInteger(0));
        gradeDistribution.put("D", new AtomicInteger(0));
        gradeDistribution.put("F", new AtomicInteger(0));
    }

    public void start() {
        this.startTimestampMillis = System.currentTimeMillis();
    }

    public void stop() {
        this.endTimestampMillis = System.currentTimeMillis();
    }

    public void recordResult(ProcessingResult result) {
        totalEvaluated.incrementAndGet();
        if (result.isSuccess()) {
            successCount.incrementAndGet();
            String grade = result.getStudentScore().getLetterGrade();
            gradeDistribution.computeIfAbsent(grade, k -> new AtomicInteger(0)).incrementAndGet();
        } else {
            failureCount.incrementAndGet();
            errorCounts.computeIfAbsent(result.getErrorCategory(), k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    public void printStructuredReport() {
        long durationMs = Math.max(1, endTimestampMillis - startTimestampMillis);
        double throughput = (totalEvaluated.get() * 1000.0) / durationMs;

        ConsoleUtil.printHeader("SEMESTER RESULT PROCESSOR - BATCH EVALUATION REPORT");
        System.out.printf("Total Files Evaluated    : %,10d%n", totalEvaluated.get());
        System.out.printf(ConsoleUtil.GREEN + "Successfully Processed   : %,10d (%.2f%%)%n" + ConsoleUtil.RESET,
                successCount.get(), (successCount.get() * 100.0) / totalEvaluated.get());
        System.out.printf(ConsoleUtil.RED + "Corrupted / Faulty Files : %,10d (%.2f%%)%n" + ConsoleUtil.RESET,
                failureCount.get(), (failureCount.get() * 100.0) / totalEvaluated.get());
        System.out.printf("Total Elapsed Time       : %,10d ms%n", durationMs);
        System.out.printf(ConsoleUtil.CYAN + "Processing Throughput    : %,10.2f files/second%n" + ConsoleUtil.RESET, throughput);

        System.out.println("\n--- [Fault Isolation & Error Category Breakdown] ---");
        for (Map.Entry<ProcessingResult.ErrorCategory, AtomicInteger> entry : errorCounts.entrySet()) {
            if (entry.getValue().get() > 0) {
                System.out.printf(" - %-26s : %,6d instances%n", entry.getKey(), entry.getValue().get());
            }
        }

        System.out.println("\n--- [Grade Distribution of Successful Submissions] ---");
        int totalPassed = 0;
        for (String grade : List.of("A", "B", "C", "D", "F")) {
            int count = gradeDistribution.getOrDefault(grade, new AtomicInteger(0)).get();
            if (!grade.equals("F")) totalPassed += count;
            System.out.printf(" - Grade %s : %,6d (%.1f%%)%n",
                    grade, count, (count * 100.0) / Math.max(1, successCount.get()));
        }
        System.out.printf("Overall Student Pass Rate : %.2f%%%n", (totalPassed * 100.0) / Math.max(1, successCount.get()));
        System.out.println("=".repeat(75));
    }

    public int getTotalEvaluated() {
        return totalEvaluated.get();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getFailureCount() {
        return failureCount.get();
    }
}
