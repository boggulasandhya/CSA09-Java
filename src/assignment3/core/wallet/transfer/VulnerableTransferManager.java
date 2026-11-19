package assignment3.core.wallet.transfer;

import assignment3.core.util.ConsoleUtil;
import assignment3.core.wallet.HostelWallet;

/**
 * Demonstrates a DEADLOCK-VULNERABLE transfer implementation.
 * Naively acquires locks in parameter order (first 'from', then 'to').
 * When two threads execute concurrent cross-transfers (A->B and B->A),
 * a classic Coffman circular wait condition occurs resulting in deadlock.
 */
public class VulnerableTransferManager {

    public static boolean transfer(HostelWallet from, HostelWallet to, double amount) {
        // VULNERABLE LOCK ORDER: Locks 'from' first, then 'to'
        synchronized (from) {
            System.out.printf("   [%s] Acquired lock on source wallet: %s%n",
                    Thread.currentThread().getName(), from.getWalletId());

            // Simulate slight delay to dramatically increase probability of deadlock
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            System.out.printf("   [%s] Waiting to acquire lock on target wallet: %s...%n",
                    Thread.currentThread().getName(), to.getWalletId());

            synchronized (to) {
                System.out.printf(ConsoleUtil.GREEN + "   [%s] Acquired both locks! Executing transfer...%n" + ConsoleUtil.RESET,
                        Thread.currentThread().getName());

                if (from.getBalance() < amount) {
                    return false;
                }
                from.debit(amount);
                to.credit(amount);
                return true;
            }
        }
    }
}
