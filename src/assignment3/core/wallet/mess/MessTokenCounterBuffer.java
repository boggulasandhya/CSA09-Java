package assignment3.core.wallet.mess;

import assignment3.core.util.ConsoleUtil;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Bounded buffer producer-consumer monitor for mess token distribution.
 * Implements strict capacity bounds (max 5 tokens) using wait(), notifyAll(),
 * and robust while-loop condition guards.
 */
public class MessTokenCounterBuffer {

    public static final int MAX_CAPACITY = 5;
    private final Queue<MessToken> buffer = new LinkedList<>();
    private boolean shutdownRequested = false;

    /**
     * Producer method: Dispenses a newly generated token into the buffer.
     * Blocks if buffer is at full capacity (5 tokens).
     */
    public synchronized void putToken(MessToken token) throws InterruptedException {
        // ROBUST CONDITION CHECK:
        // Always use while-loop instead of 'if' to guard against:
        // 1. Spurious Wakeups: JVM/OS may wake waiting thread without an explicit notify.
        // 2. State Race Conditions: Another thread might have consumed or claimed the slot
        //    between notification and re-acquiring the monitor.
        while (buffer.size() == MAX_CAPACITY && !shutdownRequested) {
            System.out.printf(ConsoleUtil.YELLOW + "   [BUFFER FULL: %d/%d] Producer '%s' waiting for space...%n" + ConsoleUtil.RESET,
                    buffer.size(), MAX_CAPACITY, Thread.currentThread().getName());
            wait(); // Releases monitor and suspends thread
        }

        if (shutdownRequested) {
            return;
        }

        buffer.add(token);
        System.out.printf(ConsoleUtil.GREEN + "   [PRODUCED] %s deposited %s | Buffer Fill: [%d/%d]%n" + ConsoleUtil.RESET,
                Thread.currentThread().getName(), token.getTokenId(), buffer.size(), MAX_CAPACITY);

        // NOTIFY ALL:
        // Use notifyAll() rather than notify() to prevent "Lost Wakeup" deadlocks.
        // notify() only wakes up one arbitrary thread; if a producer notifies another
        // producer instead of a consumer when buffer is full, both will sleep and stall the system.
        notifyAll();
    }

    /**
     * Consumer method: Acquires a token from the buffer.
     * Blocks if buffer is empty (0 tokens).
     */
    public synchronized MessToken takeToken() throws InterruptedException {
        // ROBUST CONDITION CHECK:
        while (buffer.isEmpty() && !shutdownRequested) {
            System.out.printf(ConsoleUtil.YELLOW + "   [BUFFER EMPTY: 0/%d] Student '%s' waiting for token...%n" + ConsoleUtil.RESET,
                    MAX_CAPACITY, Thread.currentThread().getName());
            wait();
        }

        if (buffer.isEmpty() && shutdownRequested) {
            return null;
        }

        MessToken token = buffer.poll();
        System.out.printf(ConsoleUtil.CYAN + "   [CONSUMED] %s acquired %s | Buffer Fill: [%d/%d]%n" + ConsoleUtil.RESET,
                Thread.currentThread().getName(), token.getTokenId(), buffer.size(), MAX_CAPACITY);

        // Notify waiting producers that a slot is now free
        notifyAll();
        return token;
    }

    public synchronized void requestShutdown() {
        this.shutdownRequested = true;
        notifyAll(); // Wake up any threads blocked in wait()
    }

    public synchronized int getCount() {
        return buffer.size();
    }

    public synchronized boolean isShutdown() {
        return shutdownRequested && buffer.isEmpty();
    }
}
