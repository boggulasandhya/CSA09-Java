package assignment3.core.atm.exceptions;

/**
 * Thrown when an invalid withdrawal amount is provided (negative, zero,
 * not conforming to note denominations, or exceeding single-transaction limits).
 */
public class InvalidAmountException extends AtmException {
    private final double attemptedAmount;
    private final String reason;

    public InvalidAmountException(String accountNumber, double attemptedAmount, String reason) {
        super(String.format("Invalid withdrawal amount $%.2f for account %s: %s",
                attemptedAmount, accountNumber, reason), accountNumber);
        this.attemptedAmount = attemptedAmount;
        this.reason = reason;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    public String getReason() {
        return reason;
    }
}
