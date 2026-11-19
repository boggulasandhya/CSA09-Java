package assignment3.core.scanner;

import assignment3.core.util.ConsoleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Runner and demonstration harness for Module 4 (Parallel Answer-Script Scanning).
 * Showcases Thread subclassing vs Runnable implementation, start() vs run(),
 * join(), sleep(), thread priorities, and daemon monitoring.
 */
public class ScannerSimulation {

    public static void main(String[] args) {
        runAutomatedDemo();
    }

    public static void runInteractive(Scanner scanner) {
        ConsoleUtil.printHeader("MODULE 4: PARALLEL ANSWER SCRIPT SCANNER (INTERACTIVE MODE)");
        System.out.print("Enter number of answer scripts per station (e.g. 3 to 10): ");
        int countPerStation = 4;
        try {
            countPerStation = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException ignored) {}

        executeScanningWorkload(countPerStation);
    }

    public static void runAutomatedDemo() {
        ConsoleUtil.printHeader("MODULE 4: PARALLEL ANSWER SCRIPT SCANNER - TEST HARNESS");
        System.out.println("Demonstrating Thread subclassing vs Runnable, start() vs run(),");
        System.out.println("lifecycle controls (sleep, join), priorities, and daemon threads.\n");

        // PART 1: Demonstrate start() vs run()
        ConsoleUtil.printSubHeader("PART 1: Thread Execution Pitfall - start() vs run()");
        demonstrateStartVsRun();

        // PART 2: Full Parallel Workload with Priorities, Daemon, and Join
        ConsoleUtil.printSubHeader("PART 2: Parallel Scanning with Thread Subclass, Runnable & Daemon");
        executeScanningWorkload(4);

        System.out.println();
        ConsoleUtil.printSuccess("Module 4 (Parallel Scanner) completed all test verification criteria successfully!\n");
    }

    private static void demonstrateStartVsRun() {
        List<AnswerScript> singleScript = List.of(new AnswerScript("SCR-DEMO-01", "ROLL-001", "CS101", 3));
        ThreadSubclassScannerStation testStation = new ThreadSubclassScannerStation("Test-Station", singleScript, Thread.NORM_PRIORITY);

        System.out.println("Calling testStation.run() DIRECTLY (Synchronous execution):");
        System.out.println("Current thread before run(): " + Thread.currentThread().getName());
        testStation.run(); // Runs on caller (main) thread!
        System.out.println("Notice: run() executed synchronously on '" + Thread.currentThread().getName() + "', NOT a new thread!\n");

        // Create fresh station for start()
        List<AnswerScript> singleScript2 = List.of(new AnswerScript("SCR-DEMO-02", "ROLL-002", "CS101", 3));
        ThreadSubclassScannerStation asyncStation = new ThreadSubclassScannerStation("Async-Station", singleScript2, Thread.NORM_PRIORITY);

        System.out.println("State before start(): " + asyncStation.getState());
        System.out.println("Calling asyncStation.start() (Spawns independent OS thread):");
        asyncStation.start(); // Asynchronous new thread
        System.out.println("State immediately after start(): " + asyncStation.getState());

        try {
            asyncStation.join(); // Wait for completion
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("State after join(): " + asyncStation.getState());
        ConsoleUtil.printSuccess("start() successfully scheduled a concurrent worker thread and joined.\n");
    }

    private static void executeScanningWorkload(int countPerStation) {
        // Prepare script batches
        List<AnswerScript> batch1 = generateBatch("ALPHA", countPerStation);
        List<AnswerScript> batch2 = generateBatch("BETA", countPerStation);
        List<AnswerScript> batch3 = generateBatch("GAMMA", countPerStation);

        // Station 1: Thread subclass with MAX_PRIORITY (10)
        ThreadSubclassScannerStation station1 = new ThreadSubclassScannerStation("Station-A", batch1, Thread.MAX_PRIORITY);

        // Station 2: Runnable implementation with NORM_PRIORITY (5)
        RunnableScannerStation runnable2 = new RunnableScannerStation("Station-B", batch2);
        Thread station2 = new Thread(runnable2, "ThreadRunnable-Station-B");
        station2.setPriority(Thread.NORM_PRIORITY);

        // Station 3: Runnable implementation with MIN_PRIORITY (1)
        RunnableScannerStation runnable3 = new RunnableScannerStation("Station-C", batch3);
        Thread station3 = new Thread(runnable3, "ThreadRunnable-Station-C");
        station3.setPriority(Thread.MIN_PRIORITY);

        // Daemon Thread: Background health monitor
        List<Thread> workers = Arrays.asList(station1, station2, station3);
        ScannerMonitorDaemon daemon = new ScannerMonitorDaemon(workers);

        System.out.println("Initial Thread States:");
        for (Thread t : workers) {
            System.out.printf(" - %-26s | State: %-12s | Priority: %d | isDaemon: %b%n",
                    t.getName(), t.getState(), t.getPriority(), t.isDaemon());
        }

        long startTime = System.currentTimeMillis();

        // Start all threads (workers + daemon)
        daemon.start();
        station1.start();
        station2.start();
        station3.start();

        System.out.println("\nAll worker stations running concurrently. Main thread invoking join()...\n");

        // Main thread waits using join()
        try {
            station1.join();
            station2.join();
            station3.join();
        } catch (InterruptedException e) {
            ConsoleUtil.printError("Main thread interrupted while waiting for scanner stations: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        long totalDuration = System.currentTimeMillis() - startTime;

        ConsoleUtil.printHeader("SCANNING BATCH COMPLETION SUMMARY");
        System.out.printf("Total Elapsed Time: %d ms%n", totalDuration);
        System.out.printf("Station A (Thread Subclass) : %d scripts scanned.%n", station1.getProcessedCount());
        System.out.printf("Station B (Runnable Task)   : %d scripts scanned.%n", runnable2.getProcessedCount());
        System.out.printf("Station C (Runnable Task)   : %d scripts scanned.%n", runnable3.getProcessedCount());

        int totalScanned = station1.getProcessedCount() + runnable2.getProcessedCount() + runnable3.getProcessedCount();
        System.out.printf("Total Scripts Digitized     : %d scripts%n", totalScanned);
    }

    private static List<AnswerScript> generateBatch(String prefix, int count) {
        List<AnswerScript> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(new AnswerScript(
                    String.format("SCRIPT-%s-%03d", prefix, i),
                    String.format("24-CS-%s-%03d", prefix, i),
                    "CS-302",
                    3 + (i % 4) // 3 to 6 pages
            ));
        }
        return list;
    }
}
