package assignment3.core.scanner;

import assignment3.core.util.ConsoleUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scanner Station implemented by directly subclassing java.lang.Thread.
 * Demonstrates thread lifecycle, Thread.sleep(), and priority handling.
 */
public class ThreadSubclassScannerStation extends Thread {

    private final String stationId;
    private final List<AnswerScript> batchQueue;
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public ThreadSubclassScannerStation(String stationId, List<AnswerScript> batchQueue, int threadPriority) {
        super("ThreadSubclass-" + stationId);
        this.stationId = stationId;
        this.batchQueue = batchQueue;
        this.setPriority(threadPriority);
    }

    @Override
    public void run() {
        System.out.println(ConsoleUtil.GREEN + "[Thread Subclass: " + getName() 
                + "] STARTED execution on thread ID: " + threadId() 
                + " with Priority: " + getPriority() + ConsoleUtil.RESET);

        for (AnswerScript script : batchQueue) {
            long startMs = System.currentTimeMillis();
            try {
                // Simulate physical page feeding and optical sensor recognition
                // Sleep duration proportional to number of pages (e.g. 15ms per page)
                long pageLatency = script.getPageCount() * 15L;
                Thread.sleep(pageLatency);

                long duration = System.currentTimeMillis() - startMs;
                script.markScanned(duration);
                processedCount.incrementAndGet();

                System.out.printf("   [%s] Scanned %s (%d pages) in %d ms [Thread State: %s]%n",
                        getName(), script.getScriptId(), script.getPageCount(), duration, getState());

            } catch (InterruptedException e) {
                System.out.println(ConsoleUtil.YELLOW + "   [" + getName() + "] Interrupted during scanning!" + ConsoleUtil.RESET);
                Thread.currentThread().interrupt(); // Restore interrupted status
                break;
            }
        }

        System.out.println(ConsoleUtil.GREEN + "[Thread Subclass: " + getName() 
                + "] FINISHED processing " + processedCount.get() + " script(s)." + ConsoleUtil.RESET);
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public String getStationId() {
        return stationId;
    }
}
