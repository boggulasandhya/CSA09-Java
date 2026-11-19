package assignment3.core.sales;

import assignment3.core.util.ConsoleUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates manual resource management using pre-Java 7 try-catch-finally patterns.
 * Highlights verbose boilerplate, nested error handling, and exception masking pitfalls.
 */
public class ManualResourceManager {

    /**
     * Reads sales records using traditional manual try-catch-finally pattern.
     */
    public List<SalesRecord> readSalesDataManual(File file) throws IOException {
        System.out.println(ConsoleUtil.MAGENTA + "[ManualResourceManager] Executing legacy try-catch-finally pattern..." + ConsoleUtil.RESET);
        
        List<SalesRecord> records = new ArrayList<>();
        BufferedReader reader = null;
        String trackerHandle = null;

        try {
            trackerHandle = ResourceTracker.registerResource("Manual-FileReader-" + file.getName());
            reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip comments/blank lines
                }
                records.add(SalesRecord.parseFromCsv(line));
            }
            ConsoleUtil.printSuccess("Manual reader parsed " + records.size() + " records successfully.");

        } catch (IOException e) {
            ConsoleUtil.printError("I/O error during manual file read: " + e.getMessage());
            throw e;

        } finally {
            // VERBOSE MANUAL CLEANUP IN FINALLY:
            // Must check for null, and must wrap close() in another try-catch!
            System.out.println(ConsoleUtil.CYAN + "-> [Manual finally block] Cleaning up reader resource..." + ConsoleUtil.RESET);
            if (reader != null) {
                try {
                    reader.close();
                    if (trackerHandle != null) {
                        ResourceTracker.releaseResource(trackerHandle);
                    }
                } catch (IOException closeEx) {
                    // Critical pitfall: If reader.close() throws here, any exception thrown in try
                    // is COMPLETELY MASKED and LOST unless manually handled!
                    ConsoleUtil.printError("Exception occurred inside finally during reader.close(): " + closeEx.getMessage());
                }
            }
        }

        return records;
    }

    /**
     * Demonstrates exception masking where a finally block exception silently discards
     * the original try-block exception in manual try-catch-finally.
     */
    public void demonstrateExceptionMasking() {
        System.out.println(ConsoleUtil.YELLOW + "=== [Demonstrating Legacy Exception Masking Pitfall] ===" + ConsoleUtil.RESET);
        try {
            try {
                System.out.println("1. Try block starts: Encountering corrupted data stream...");
                throw new IOException("PRIMARY ERROR: Corrupted sales payload at byte 4096.");
            } finally {
                System.out.println("2. Finally block starts: Cleaning up faulty resource...");
                // Simulating a secondary exception thrown during cleanup
                throw new IOException("SECONDARY ERROR: Network socket flush failed during close().");
            }
        } catch (IOException caughtEx) {
            System.out.println(ConsoleUtil.RED + "[MASKING OCCURRED] The caller caught: " + caughtEx.getMessage() + ConsoleUtil.RESET);
            System.out.println(ConsoleUtil.RED + "Notice: The PRIMARY ERROR was silently lost and swallowed!" + ConsoleUtil.RESET);
            System.out.println("Suppressed count: " + caughtEx.getSuppressed().length);
        }
    }

    /**
     * Simulates an unmanaged leak where resources are not closed properly.
     */
    public void simulateUnmanagedResourceLeak() {
        System.out.println(ConsoleUtil.YELLOW + "=== [Simulating Unmanaged Resource Leak] ===" + ConsoleUtil.RESET);
        String handle = ResourceTracker.registerResource("Leaked-File-Stream-001");
        System.out.println("Simulating code path where developer omitted close() or exception skipped cleanup.");
        // Does not call releaseResource() to demonstrate leak detection
    }
}
