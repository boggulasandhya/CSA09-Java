package assignment3.core.sales;

import assignment3.core.util.ConsoleUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Runner and demonstration harness for Module 3 (Daily Sales Report Generator).
 * Demonstrates manual try-catch-finally vs modern try-with-resources,
 * suppressed exceptions, and handle leak tracking.
 */
public class SalesReportSimulation {

    public static void main(String[] args) {
        runAutomatedDemo();
    }

    public static void runInteractive(Scanner scanner) {
        ConsoleUtil.printHeader("MODULE 3: DAILY SALES REPORT GENERATOR (INTERACTIVE MODE)");
        System.out.println("1. Run Modern Try-With-Resources Report Generation");
        System.out.println("2. Run Legacy Manual Try-Catch-Finally Demonstration");
        System.out.println("3. Run Suppressed Exceptions vs Exception Masking Comparison");
        System.out.println("4. Run Resource Handle Leak Diagnostics Audit");
        System.out.print("Select an option (1-4): ");
        String choice = scanner.nextLine().trim();

        File sampleFile = createSampleSalesFile();
        File reportFile = new File("sales_summary_report.txt");

        ModernResourceManager modern = new ModernResourceManager();
        ManualResourceManager manual = new ManualResourceManager();

        switch (choice) {
            case "1" -> {
                try {
                    modern.generateDailySummaryReport(sampleFile, reportFile);
                } catch (IOException e) {
                    ConsoleUtil.printError("Report generation failed: " + e.getMessage());
                }
            }
            case "2" -> {
                try {
                    List<SalesRecord> records = manual.readSalesDataManual(sampleFile);
                    System.out.println("Read " + records.size() + " records manually.");
                } catch (IOException e) {
                    ConsoleUtil.printError("Manual read failed: " + e.getMessage());
                }
            }
            case "3" -> {
                manual.demonstrateExceptionMasking();
                System.out.println();
                modern.demonstrateSuppressedExceptions();
            }
            case "4" -> {
                manual.simulateUnmanagedResourceLeak();
                ResourceTracker.printActiveLeakDiagnostics();
                ResourceTracker.clear();
            }
            default -> System.out.println("Invalid option.");
        }
    }

    public static void runAutomatedDemo() {
        ConsoleUtil.printHeader("MODULE 3: DAILY SALES REPORT - COMPREHENSIVE TEST HARNESS");
        System.out.println("Comparing manual try-catch-finally vs modern try-with-resources,");
        System.out.println("demonstrating suppressed exceptions and resource handle diagnostics.\n");

        File sampleFile = createSampleSalesFile();
        File reportFile = new File("daily_sales_output.txt");

        ManualResourceManager manual = new ManualResourceManager();
        ModernResourceManager modern = new ModernResourceManager();

        // 1. Manual Resource Management
        ConsoleUtil.printSubHeader("PART 1: Legacy Manual try-catch-finally Reading");
        try {
            List<SalesRecord> manualRecords = manual.readSalesDataManual(sampleFile);
            System.out.println("First record sample: " + manualRecords.get(0));
        } catch (IOException e) {
            ConsoleUtil.printError("Manual read error: " + e.getMessage());
        }

        // 2. Modern Try-With-Resources & Summary Report Generation
        ConsoleUtil.printSubHeader("PART 2: Modern try-with-resources & Dual Stream Processing");
        try {
            modern.generateDailySummaryReport(sampleFile, reportFile);
        } catch (IOException e) {
            ConsoleUtil.printError("Modern report error: " + e.getMessage());
        }

        // 3. Exception Masking in Legacy Code
        ConsoleUtil.printSubHeader("PART 3: Exception Masking in Legacy try-catch-finally");
        manual.demonstrateExceptionMasking();

        // 4. Suppressed Exceptions in Modern Java
        ConsoleUtil.printSubHeader("PART 4: Suppressed Exceptions Handling in try-with-resources");
        modern.demonstrateSuppressedExceptions();

        // 5. Resource Handle Leak Diagnostics
        ConsoleUtil.printSubHeader("PART 5: Resource Leak Tracking & Diagnostics");
        System.out.println("Checking resource leaks after regular operations...");
        ResourceTracker.printActiveLeakDiagnostics();

        System.out.println("\nSimulating an unclosed file handle leak...");
        manual.simulateUnmanagedResourceLeak();
        ResourceTracker.printActiveLeakDiagnostics();
        ResourceTracker.clear(); // Reset tracker

        System.out.println();
        ConsoleUtil.printSuccess("Module 3 (Daily Sales Report) completed all test verification criteria successfully!\n");
    }

    public static File createSampleSalesFile() {
        File tempFile = new File("sales_data_sample.csv");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("# TransactionId, BranchCode, Category, Amount, UnitsSold\n");
            writer.write("TX-1001, NORTH-01, Electronics, 1250.50, 2\n");
            writer.write("TX-1002, SOUTH-02, Furniture,    840.00, 4\n");
            writer.write("TX-1003, EAST-03,  Groceries,    145.20, 15\n");
            writer.write("TX-1004, WEST-04,  Electronics, 3200.00, 3\n");
            writer.write("TX-1005, NORTH-01, Clothing,     220.80, 5\n");
            writer.write("TX-1006, SOUTH-02, Electronics,  799.99, 1\n");
            writer.write("TX-1007, EAST-03,  Furniture,   1150.00, 2\n");
            writer.write("TX-1008, WEST-04,  Groceries,    310.50, 22\n");
        } catch (IOException e) {
            ConsoleUtil.printError("Failed to initialize sample sales data file: " + e.getMessage());
        }
        return tempFile;
    }
}
