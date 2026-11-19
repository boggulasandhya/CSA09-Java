package assignment3.core.atm.exceptions;

/**
 * Thrown when an account is locked due to too many invalid PIN attempts.
 */
public class AccountLockedException extends AtmException {
    private final int failedAttempts;

    public AccountLockedException(String accountNumber, int failedAttempts) {
        super(String.format("Account %s is locked due to %d consecutive failed PIN attempts.",
                accountNumber, failedAttempts), accountNumber);
        this.failedAttempts = failedAttempts;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }
}
