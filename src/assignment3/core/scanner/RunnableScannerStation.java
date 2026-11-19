package assignment3.core.scanner;

import assignment3.core.util.ConsoleUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scanner Station implemented via the java.lang.Runnable interface.
 * Decouples the scanning workload from thread lifecycle management.
 */
public class RunnableScannerStation implements Runnable {

    private final String stationId;
    private final List<AnswerScript> batchQueue;
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public RunnableScannerStation(String stationId, List<AnswerScript> batchQueue) {
        this.stationId = stationId;
        this.batchQueue = batchQueue;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println(ConsoleUtil.CYAN + "[Runnable Task: " + stationId 
                + "] STARTED on Thread: '" + threadName 
                + "' (Priority: " + Thread.currentThread().getPriority() + ")" + ConsoleUtil.RESET);

        for (AnswerScript script : batchQueue) {
            long startMs = System.currentTimeMillis();
            try {
                // Simulate optical OCR scanning latency
                long pageLatency = script.getPageCount() * 12L;
                Thread.sleep(pageLatency);

                long duration = System.currentTimeMillis() - startMs;
                script.markScanned(duration);
                processedCount.incrementAndGet();

                System.out.printf("   [%s / %s] Scanned %s (%d pages) in %d ms [State: %s]%n",
                        stationId, threadName, script.getScriptId(), script.getPageCount(), duration,
                        Thread.currentThread().getState());

            } catch (InterruptedException e) {
                System.out.println(ConsoleUtil.YELLOW + "   [" + stationId + "] Interrupted!" + ConsoleUtil.RESET);
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println(ConsoleUtil.CYAN + "[Runnable Task: " + stationId 
                + "] FINISHED processing " + processedCount.get() + " script(s)." + ConsoleUtil.RESET);
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public String getStationId() {
        return stationId;
    }
}
