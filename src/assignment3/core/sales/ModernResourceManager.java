package assignment3.core.sales;

import assignment3.core.util.ConsoleUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates modern resource management using Java 7+ try-with-resources.
 * Showcases automatic deterministic resource cleanup, reverse-order closure,
 * and suppressed exception handling.
 */
public class ModernResourceManager {

    /**
     * Reads sales records using modern try-with-resources.
     */
    public List<SalesRecord> readSalesDataModern(File file) throws IOException {
        System.out.println(ConsoleUtil.MAGENTA + "[ModernResourceManager] Executing try-with-resources pattern..." + ConsoleUtil.RESET);
        
        List<SalesRecord> records = new ArrayList<>();
        String trackerHandle = ResourceTracker.registerResource("Modern-FileReader-" + file.getName());

        // TRY-WITH-RESOURCES: Automatically closes reader upon exiting the block
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                records.add(SalesRecord.parseFromCsv(line));
            }
            ConsoleUtil.printSuccess("Modern reader parsed " + records.size() + " records successfully.");
        } finally {
            // Note: ResourceTracker registration release is performed here for accounting
            ResourceTracker.releaseResource(trackerHandle);
        }

        return records;
    }

    /**
     * Generates a compiled daily sales summary and writes it to an output file
     * using dual-resource try-with-resources (FileReader + FileWriter).
     */
    public void generateDailySummaryReport(File inputFile, File outputFile) throws IOException {
        System.out.println(ConsoleUtil.MAGENTA + "[ModernResourceManager] Compiling daily summary with dual try-with-resources..." + ConsoleUtil.RESET);

        String readHandle = ResourceTracker.registerResource("Summary-Reader-" + inputFile.getName());
        String writeHandle = ResourceTracker.registerResource("Summary-Writer-" + outputFile.getName());

        // Multiple resources managed simultaneously; closed in reverse order of initialization
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            double totalRevenue = 0.0;
            int totalUnits = 0;
            Map<String, Double> categoryRevenue = new HashMap<>();
            Map<String, Double> branchRevenue = new HashMap<>();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                SalesRecord record = SalesRecord.parseFromCsv(line);
                totalRevenue += record.getAmount();
                totalUnits += record.getUnitsSold();

                categoryRevenue.merge(record.getCategory(), record.getAmount(), Double::sum);
                branchRevenue.merge(record.getBranchCode(), record.getAmount(), Double::sum);
            }

            // Write report header
            writer.write("=====================================================\n");
            writer.write("           DAILY SALES SUMMARY REPORT               \n");
            writer.write("=====================================================\n");
            writer.write(String.format("Total Revenue:  $%,.2f\n", totalRevenue));
            writer.write(String.format("Total Units:    %,d\n", totalUnits));
            writer.write("-----------------------------------------------------\n");
            writer.write("Category Breakdown:\n");
            for (Map.Entry<String, Double> entry : categoryRevenue.entrySet()) {
                writer.write(String.format(" - %-15s : $%,10.2f (%.1f%%)\n",
                        entry.getKey(), entry.getValue(), (entry.getValue() / totalRevenue) * 100));
            }
            writer.write("-----------------------------------------------------\n");
            writer.write("Branch Breakdown:\n");
            for (Map.Entry<String, Double> entry : branchRevenue.entrySet()) {
                writer.write(String.format(" - %-15s : $%,10.2f\n", entry.getKey(), entry.getValue()));
            }
            writer.write("=====================================================\n");
            writer.flush();

            ConsoleUtil.printSuccess("Summary report written to: " + outputFile.getAbsolutePath());

        } finally {
            ResourceTracker.releaseResource(writeHandle);
            ResourceTracker.releaseResource(readHandle);
        }
    }

    /**
     * Demonstrates suppressed exception capturing with try-with-resources.
     * Both data processing AND closing fail; the JVM captures the close error
     * inside the primary exception's getSuppressed() array!
     */
    public void demonstrateSuppressedExceptions() {
        System.out.println(ConsoleUtil.YELLOW + "=== [Demonstrating Try-With-Resources Suppressed Exceptions] ===" + ConsoleUtil.RESET);

        // FaultyResource throws IOException on processData() AND on close()
        try (FaultyResource resource = new FaultyResource("FaultyDiskBuffer", true, true)) {
            System.out.println("1. Processing faulty resource (will throw during read)...");
            resource.processData();

        } catch (IOException primaryEx) {
            System.out.println(ConsoleUtil.GREEN + "[PRIMARY EXCEPTION PRESERVED]: " + primaryEx.getMessage() + ConsoleUtil.RESET);

            Throwable[] suppressed = primaryEx.getSuppressed();
            System.out.println("   Suppressed Exception Count: " + suppressed.length);

            for (int i = 0; i < suppressed.length; i++) {
                System.out.println(ConsoleUtil.CYAN + "   [Suppressed #" + (i + 1) + "] Type: " 
                        + suppressed[i].getClass().getSimpleName() + ConsoleUtil.RESET);
                System.out.println(ConsoleUtil.CYAN + "      Message: " + suppressed[i].getMessage() + ConsoleUtil.RESET);
            }

            ConsoleUtil.printSuccess("Notice: Both exceptions were captured! The primary error was NOT masked.");
        }
    }
}
