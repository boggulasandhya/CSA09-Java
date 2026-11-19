package assignment3.core.scanner;

import assignment3.core.util.ConsoleUtil;

import java.util.List;

/**
 * Background daemon thread performing periodic health and throughput monitoring
 * across all active scanner stations.
 * Automatically terminates when all non-daemon user threads finish.
 */
public class ScannerMonitorDaemon extends Thread {

    private final List<Thread> stationsToMonitor;

    public ScannerMonitorDaemon(List<Thread> stationsToMonitor) {
        super("ScannerMonitor-Daemon");
        this.stationsToMonitor = stationsToMonitor;
        // CRITICAL: Configure as daemon thread before start() is invoked!
        this.setDaemon(true);
        this.setPriority(Thread.MIN_PRIORITY);
    }

    @Override
    public void run() {
        System.out.println(ConsoleUtil.MAGENTA + "[Daemon: " + getName() 
                + "] STARTED (isDaemon=" + isDaemon() + "). Providing background telemetry." + ConsoleUtil.RESET);

        int tick = 1;
        while (true) {
            try {
                Thread.sleep(60); // Telemetry sample interval

                long activeCount = stationsToMonitor.stream().filter(Thread::isAlive).count();
                System.out.printf(ConsoleUtil.MAGENTA + "   [DAEMON TELEMETRY #%02d] Active Worker Threads: %d / %d | Heap Used: %,d KB%n" + ConsoleUtil.RESET,
                        tick++, activeCount, stationsToMonitor.size(),
                        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024);

                // If all monitored worker threads have completed, daemon can finish or keep looping
                // until JVM shuts down
                if (activeCount == 0) {
                    System.out.println(ConsoleUtil.MAGENTA + "   [DAEMON] All scanner worker stations have terminated. Daemon standing down." + ConsoleUtil.RESET);
                    break;
                }

            } catch (InterruptedException e) {
                System.out.println("[DAEMON] Monitor interrupted.");
                break;
            }
        }
    }
}
