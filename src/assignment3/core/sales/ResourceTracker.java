package assignment3.core.sales;

import assignment3.core.util.ConsoleUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks open file descriptors and system resources to diagnose and detect resource leaks.
 */
public class ResourceTracker {

    private static final Map<String, ResourceDescriptor> OPEN_RESOURCES = new ConcurrentHashMap<>();
    private static final AtomicInteger RESOURCE_COUNTER = new AtomicInteger(0);

    public static class ResourceDescriptor {
        private final String resourceId;
        private final String resourceName;
        private final long openedAtMillis;
        private final StackTraceElement[] allocationStackTrace;

        public ResourceDescriptor(String resourceId, String resourceName, StackTraceElement[] stackTrace) {
            this.resourceId = resourceId;
            this.resourceName = resourceName;
            this.openedAtMillis = System.currentTimeMillis();
            this.allocationStackTrace = stackTrace;
        }

        public String getResourceId() {
            return resourceId;
        }

        public String getResourceName() {
            return resourceName;
        }

        public long getOpenedAtMillis() {
            return openedAtMillis;
        }

        public StackTraceElement[] getAllocationStackTrace() {
            return allocationStackTrace;
        }
    }

    public static String registerResource(String resourceName) {
        String id = "RES-HANDLE-" + RESOURCE_COUNTER.incrementAndGet();
        OPEN_RESOURCES.put(id, new ResourceDescriptor(id, resourceName, Thread.currentThread().getStackTrace()));
        System.out.println(ConsoleUtil.CYAN + "   [ResourceTracker] OPENED: " + id + " (" + resourceName + ")" + ConsoleUtil.RESET);
        return id;
    }

    public static void releaseResource(String resourceId) {
        ResourceDescriptor desc = OPEN_RESOURCES.remove(resourceId);
        if (desc != null) {
            System.out.println(ConsoleUtil.GREEN + "   [ResourceTracker] CLOSED: " + resourceId + " (" + desc.getResourceName() + ")" + ConsoleUtil.RESET);
        } else {
            System.out.println(ConsoleUtil.YELLOW + "   [ResourceTracker] Attempted double-close or untracked release on: " + resourceId + ConsoleUtil.RESET);
        }
    }

    public static int getActiveHandleCount() {
        return OPEN_RESOURCES.size();
    }

    public static void printActiveLeakDiagnostics() {
        if (OPEN_RESOURCES.isEmpty()) {
            ConsoleUtil.printSuccess("[ResourceTracker] Audit Passed: 0 active file handles detected (Zero Leaks).");
            return;
        }

        ConsoleUtil.printError("[ResourceTracker] LEAK WARNING: Detected " + OPEN_RESOURCES.size() + " UNCLOSED file handle(s)!");
        for (Map.Entry<String, ResourceDescriptor> entry : OPEN_RESOURCES.entrySet()) {
            ResourceDescriptor desc = entry.getValue();
            System.out.println(ConsoleUtil.RED + "   * Leaked Handle: " + desc.getResourceId() + " -> " + desc.getResourceName() + ConsoleUtil.RESET);
            System.out.println("     Open Duration: " + (System.currentTimeMillis() - desc.getOpenedAtMillis()) + " ms");
            if (desc.getAllocationStackTrace().length > 3) {
                System.out.println("     Allocation Origin: " + desc.getAllocationStackTrace()[3]);
            }
        }
    }

    public static void clear() {
        OPEN_RESOURCES.clear();
    }
}
