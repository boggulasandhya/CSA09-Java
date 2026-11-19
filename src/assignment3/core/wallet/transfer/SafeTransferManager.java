package assignment3.core.wallet.transfer;

import assignment3.core.util.ConsoleUtil;
import assignment3.core.wallet.HostelWallet;

/**
 * Production DEADLOCK-FREE transfer implementation.
 * Enforces a strict global lock acquisition ordering based on unique wallet IDs.
 * By eliminating the Coffman circular wait condition, deadlocks are mathematically impossible.
 */
public class SafeTransferManager {

    /**
     * Executes transfer with strict lock ordering.
     */
    public static boolean transfer(HostelWallet from, HostelWallet to, double amount) {
        if (from == to || from.getWalletId().equals(to.getWalletId())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same wallet account.");
        }

        // STRICT LOCK ORDERING:
        // Always acquire locks in ascending lexicographical order of wallet IDs.
        HostelWallet firstLock;
        HostelWallet secondLock;

        if (from.compareTo(to) < 0) {
            firstLock = from;
            secondLock = to;
        } else {
            firstLock = to;
            secondLock = from;
        }

        // Acquire first lock according to canonical ordering
        synchronized (firstLock) {
            // Acquire second lock
            synchronized (secondLock) {
                if (from.getBalance() < amount) {
                    return false; // Insufficient balance
                }
                from.debit(amount);
                to.credit(amount);
                return true;
            }
        }
    }
}
