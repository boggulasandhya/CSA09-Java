package assignment3.core.atm.exceptions;

import java.time.LocalDateTime;

/**
 * Base checked exception for ATM-related errors.
 */
public class AtmException extends Exception {
    private final String accountNumber;
    private final LocalDateTime timestamp;

    public AtmException(String message, String accountNumber) {
        super(message);
        this.accountNumber = accountNumber;
        this.timestamp = LocalDateTime.now();
    }

    public AtmException(String message, String accountNumber, Throwable cause) {
        super(message, cause);
        this.accountNumber = accountNumber;
        this.timestamp = LocalDateTime.now();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
