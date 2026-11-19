package assignment3.core.wallet;

/**
 * Domain entity representing a student hostel wallet account.
 */
public class HostelWallet implements Comparable<HostelWallet> {
    private final String walletId;
    private final String studentName;
    private double balance;

    public HostelWallet(String walletId, String studentName, double initialBalance) {
        this.walletId = walletId;
        this.studentName = studentName;
        this.balance = initialBalance;
    }

    public void debit(double amount) {
        if (amount <= 0 || amount > balance) {
            throw new IllegalArgumentException("Insufficient funds or invalid debit amount: " + amount);
        }
        this.balance -= amount;
    }

    public void credit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Invalid credit amount: " + amount);
        }
        this.balance += amount;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getStudentName() {
        return studentName;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public int compareTo(HostelWallet other) {
        return this.walletId.compareTo(other.walletId);
    }

    @Override
    public String toString() {
        return String.format("[%s - %s | Balance: $%.2f]", walletId, studentName, balance);
    }
}
