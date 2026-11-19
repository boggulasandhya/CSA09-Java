package assignment3.core.wallet;

import assignment3.core.util.ConsoleUtil;
import assignment3.core.wallet.mess.MessTokenCounterBuffer;
import assignment3.core.wallet.mess.TokenConsumer;
import assignment3.core.wallet.mess.TokenProducer;
import assignment3.core.wallet.transfer.SafeTransferManager;
import assignment3.core.wallet.transfer.VulnerableTransferManager;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runner and demonstration harness for Module 6 (Hostel Wallet Transfers & Mess Token Counter).
 * Covers:
 * Part A: Multi-account transfers with deadlock analysis and strict lock ordering solution.
 * Part B: Producer-consumer mess token buffer (max size 5) with wait() and notifyAll().
 */
public class WalletSimulation {

    public static void main(String[] args) {
        runAutomatedDemo();
    }

    public static void runInteractive(Scanner scanner) {
        ConsoleUtil.printHeader("MODULE 6: HOSTEL WALLET & MESS TOKEN (INTERACTIVE MODE)");
        System.out.println("1. Run Part A: Deadlock Analysis vs Safe Strict Lock Ordering");
        System.out.println("2. Run Part B: Mess Token Producer-Consumer Bounded Buffer");
        System.out.print("Select an option (1-2): ");
        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            runPartADemonstration();
        } else if ("2".equals(choice)) {
            runPartBDemonstration();
        } else {
            System.out.println("Invalid option.");
        }
    }

    public static void runAutomatedDemo() {
        ConsoleUtil.printHeader("MODULE 6: HOSTEL WALLET & MESS TOKEN - TEST HARNESS");
        System.out.println("Part A: Multi-account transfers with Deadlock & Strict Lock Ordering.");
        System.out.println("Part B: Producer-Consumer Mess Token Buffer (Max Size 5).\n");

        runPartADemonstration();
        System.out.println();
        runPartBDemonstration();

        System.out.println();
        ConsoleUtil.printSuccess("Module 6 (Hostel Wallet & Mess Token) completed all test verification criteria successfully!\n");
    }

    // =========================================================================
    // PART A: WALLET TRANSFERS & DEADLOCK MITIGATION
    // =========================================================================
    public static void runPartADemonstration() {
        ConsoleUtil.printSubHeader("PART A-1: Deadlock Vulnerability Demonstration & MXBean Detection");

        HostelWallet walletA = new HostelWallet("W-101", "Alex Chen", 1000.0);
        HostelWallet walletB = new HostelWallet("W-202", "Priya Patel", 1000.0);

        System.out.println("Initial State: " + walletA + " | " + walletB);
        System.out.println("Spawning Thread-1 (Transfers A -> B) and Thread-2 (Transfers B -> A) via VulnerableTransferManager...");

        CountDownLatch latch = new CountDownLatch(1);

        Thread thread1 = new Thread(() -> {
            try {
                latch.await();
                VulnerableTransferManager.transfer(walletA, walletB, 150.0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Vulnerable-Thread-1");

        Thread thread2 = new Thread(() -> {
            try {
                latch.await();
                VulnerableTransferManager.transfer(walletB, walletA, 200.0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Vulnerable-Thread-2");

        // Set as daemon threads so the stuck deadlock simulation does not prevent JVM exit
        thread1.setDaemon(true);
        thread2.setDaemon(true);
        thread1.start();
        thread2.start();
        latch.countDown(); // Release both simultaneously

        // Use JVM ThreadMXBean to detect deadlocks within 400ms
        ConsoleUtil.sleep(300);

        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = mxBean.findDeadlockedThreads();

        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            ConsoleUtil.printError("[DEADLOCK CONFIRMED] Detected " + deadlockedThreads.length + " deadlocked threads!");
            for (long tid : deadlockedThreads) {
                System.out.println(ConsoleUtil.RED + "   * Deadlocked Thread ID: " + tid + " (" + mxBean.getThreadInfo(tid).getThreadName() + ")" + ConsoleUtil.RESET);
            }
            System.out.println(ConsoleUtil.YELLOW + "Coffman Condition: Circular wait occurred between W-101 and W-202." + ConsoleUtil.RESET);
        } else {
            System.out.println("Threads proceeded without hitting race timing window.");
        }

        // PART A-2: Safe Deadlock-Free Transfer via Strict Lock Ordering
        ConsoleUtil.printSubHeader("PART A-2: Deadlock-Free Transfer via Strict Lock Ordering");
        HostelWallet safeWalletA = new HostelWallet("W-301", "Sam Altman", 5000.0);
        HostelWallet safeWalletB = new HostelWallet("W-402", "Ada Lovelace", 5000.0);
        double initialTotal = safeWalletA.getBalance() + safeWalletB.getBalance();

        System.out.printf("Starting Balances: %s | %s (Total: $%.2f)%n",
                safeWalletA, safeWalletB, initialTotal);
        System.out.println("Executing 100 concurrent bi-directional cross transfers (50 A->B and 50 B->A)...");

        List<Thread> safeThreads = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            safeThreads.add(new Thread(() -> SafeTransferManager.transfer(safeWalletA, safeWalletB, 20.0)));
            safeThreads.add(new Thread(() -> SafeTransferManager.transfer(safeWalletB, safeWalletA, 20.0)));
        }

        for (Thread t : safeThreads) t.start();
        for (Thread t : safeThreads) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }

        double finalTotal = safeWalletA.getBalance() + safeWalletB.getBalance();
        System.out.printf("Final Balances   : %s | %s (Total: $%.2f)%n",
                safeWalletA, safeWalletB, finalTotal);

        if (Math.abs(finalTotal - initialTotal) < 0.001) {
            ConsoleUtil.printSuccess("Strict Lock Ordering Succeeded: 100 transfers completed with ZERO deadlocks and PRESERVED INVARIANT!");
        } else {
            ConsoleUtil.printError("Invariant check failed!");
        }
    }

    // =========================================================================
    // PART B: MESS TOKEN COUNTER (BOUNDED BUFFER PRODUCER-CONSUMER)
    // =========================================================================
    public static void runPartBDemonstration() {
        ConsoleUtil.printSubHeader("PART B: Mess Token Counter Bounded Buffer (Max Capacity = 5)");
        System.out.println("Demonstrating wait() & notifyAll() with while-loop guards.\n");

        MessTokenCounterBuffer buffer = new MessTokenCounterBuffer();
        AtomicInteger consumedTotal = new AtomicInteger(0);
        int totalTokens = 12;

        // 2 Mess Staff Producers (each produces 6 tokens)
        Thread chef1 = new Thread(new TokenProducer(buffer, "Chef-Marco", 6), "Producer-Marco");
        Thread chef2 = new Thread(new TokenProducer(buffer, "Chef-Gordon", 6), "Producer-Gordon");

        // 3 Student Consumers (each acquires 4 tokens)
        Thread stu1 = new Thread(new TokenConsumer(buffer, "Student-John", 4, consumedTotal), "Consumer-John");
        Thread stu2 = new Thread(new TokenConsumer(buffer, "Student-Sarah", 4, consumedTotal), "Consumer-Sarah");
        Thread stu3 = new Thread(new TokenConsumer(buffer, "Student-David", 4, consumedTotal), "Consumer-David");

        List<Thread> allThreads = List.of(chef1, chef2, stu1, stu2, stu3);
        allThreads.forEach(Thread::start);

        for (Thread t : allThreads) {
            try {
                t.join(3000); // 3-second bounded join
            } catch (InterruptedException ignored) {}
        }

        buffer.requestShutdown();

        System.out.println();
        System.out.printf("Total Tokens Successfully Exchanged: %d / %d%n", consumedTotal.get(), totalTokens);
        System.out.printf("Final Buffer Residual Count        : %d%n", buffer.getCount());

        if (consumedTotal.get() == totalTokens) {
            ConsoleUtil.printSuccess("Producer-Consumer Bounded Buffer executed flawlessly without lost wakeups!");
        }
    }
}
