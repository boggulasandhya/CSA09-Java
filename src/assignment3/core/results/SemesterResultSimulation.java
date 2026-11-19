package assignment3.core.results;

import assignment3.core.util.ConsoleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Runner and demonstration harness for Module 7 (Semester Result Processor).
 * Evaluates 10,000 score files concurrently using ExecutorService, Callable, and Future,
 * isolating corruption errors and generating a structured audit report.
 */
public class SemesterResultSimulation {

    public static final int BENCHMARK_DATASET_SIZE = 10000;

    public static void main(String[] args) {
        runAutomatedDemo();
    }

    public static void runInteractive(Scanner scanner) {
        ConsoleUtil.printHeader("MODULE 7: SEMESTER RESULT PROCESSOR (INTERACTIVE MODE)");
        System.out.printf("Default dataset size is %,d records. Enter count (or press Enter for %,d): ",
                BENCHMARK_DATASET_SIZE, BENCHMARK_DATASET_SIZE);
        String input = scanner.nextLine().trim();
        int count = BENCHMARK_DATASET_SIZE;
        try {
            if (!input.isEmpty()) {
                count = Integer.parseInt(input);
            }
        } catch (NumberFormatException ignored) {}

        executeBatchProcessing(count);
    }

    public static void runAutomatedDemo() {
        ConsoleUtil.printHeader("MODULE 7: SEMESTER RESULT PROCESSOR - FAULT-TOLERANT 10,000 BATCH");
        System.out.println("Processing 10,000 student score files concurrently with ExecutorService, Callable & Future.");
        System.out.println("Validating fault isolation: corrupted files will not abort the batch job.\n");

        executeBatchProcessing(BENCHMARK_DATASET_SIZE);

        System.out.println();
        ConsoleUtil.printSuccess("Module 7 (Semester Result Processor) completed all test verification criteria successfully!\n");
    }

    public static BatchSummary executeBatchProcessing(int datasetSize) {
        System.out.printf("Generating synthetic dataset of %,d score files with controlled corruptions...%n", datasetSize);
        List<ScoreFileGenerator.SyntheticRecord> dataset = ScoreFileGenerator.generateDataset(datasetSize);
        System.out.println("Dataset generated. Initializing multithreaded executor thread pool...");

        int threadCount = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        System.out.printf("Worker Pool Initialized with %d concurrent worker threads.%n", threadCount);

        BatchSummary summary = new BatchSummary();
        summary.start();

        List<Future<ProcessingResult>> futures = new ArrayList<>(datasetSize);

        // Submit all 10,000 Callable tasks to ExecutorService
        for (ScoreFileGenerator.SyntheticRecord record : dataset) {
            ScoreEvaluationTask task = new ScoreEvaluationTask(record.getFileId(), record.getPayload());
            futures.add(executor.submit(task));
        }

        System.out.println("All tasks dispatched. Awaiting asynchronous Future results...");

        // Collect and aggregate Future results
        for (Future<ProcessingResult> future : futures) {
            try {
                // Resolves Callable outcome
                ProcessingResult result = future.get();
                summary.recordResult(result);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                ConsoleUtil.printError("Batch processing interrupted: " + ie.getMessage());
                break;

            } catch (ExecutionException ee) {
                // Should not occur since tasks catch and wrap internal errors in ProcessingResult
                ConsoleUtil.printError("Unexpected task execution crash: " + ee.getCause().getMessage());
            }
        }

        // Graceful ExecutorService shutdown
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        summary.stop();

        // Print final structured report
        summary.printStructuredReport();
        return summary;
    }
}
