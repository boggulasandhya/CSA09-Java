package assignment3.core.atm;

import assignment3.core.atm.exceptions.AccountLockedException;
import assignment3.core.atm.exceptions.InsufficientBalanceException;
import assignment3.core.atm.exceptions.InvalidAmountException;

/**
 * Represents a bank account with balance tracking and transactional validation rules.
 */
public class BankAccount {
    public static final double MAX_SINGLE_WITHDRAWAL = 2000.0;
    public static final int NOTE_DENOMINATION = 10;
    private static final int MAX_PIN_ATTEMPTS = 3;

    private final String accountNumber;
    private final String accountHolder;
    private final String pin;
    private double balance;
    private int failedPinAttempts;
    private boolean locked;

    public BankAccount(String accountNumber, String accountHolder, String pin, double initialBalance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.pin = pin;
        this.balance = initialBalance;
        this.failedPinAttempts = 0;
        this.locked = false;
    }

    public synchronized void authenticate(String enteredPin) throws AccountLockedException, InvalidAmountException {
        if (locked) {
            throw new AccountLockedException(accountNumber, failedPinAttempts);
        }
        if (!this.pin.equals(enteredPin)) {
            failedPinAttempts++;
            if (failedPinAttempts >= MAX_PIN_ATTEMPTS) {
                locked = true;
                throw new AccountLockedException(accountNumber, failedPinAttempts);
            }
            throw new InvalidAmountException(accountNumber, 0,
                    "Invalid PIN. Remaining attempts: " + (MAX_PIN_ATTEMPTS - failedPinAttempts));
        }
        // Reset failed attempts upon successful login
        failedPinAttempts = 0;
    }

    /**
     * Executes withdrawal with rigorous business checks.
     */
    public synchronized void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new InvalidAmountException(accountNumber, amount, "Withdrawal amount must be strictly positive.");
        }
        if (amount % NOTE_DENOMINATION != 0) {
            throw new InvalidAmountException(accountNumber, amount,
                    "Amount must be a multiple of $" + NOTE_DENOMINATION + " currency notes.");
        }
        if (amount > MAX_SINGLE_WITHDRAWAL) {
            throw new InvalidAmountException(accountNumber, amount,
                    "Amount exceeds per-transaction limit of $" + MAX_SINGLE_WITHDRAWAL);
        }
        if (amount > balance) {
            throw new InsufficientBalanceException(accountNumber, balance, amount);
        }

        this.balance -= amount;
    }

    public synchronized void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException(accountNumber, amount, "Deposit amount must be positive.");
        }
        this.balance += amount;
    }

    public synchronized double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public boolean isLocked() {
        return locked;
    }
}
