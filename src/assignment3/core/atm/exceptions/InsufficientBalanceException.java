package assignment3.core.atm.exceptions;

/**
 * Thrown when an account does not have sufficient funds for a withdrawal.
 */
public class InsufficientBalanceException extends AtmException {
    private final double currentBalance;
    private final double requestedAmount;
    private final double shortfall;

    public InsufficientBalanceException(String accountNumber, double currentBalance, double requestedAmount) {
        super(String.format("Insufficient funds for account %s: requested $%.2f, available $%.2f (shortfall: $%.2f)",
                accountNumber, requestedAmount, currentBalance, (requestedAmount - currentBalance)),
              accountNumber);
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
        this.shortfall = requestedAmount - currentBalance;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public double getShortfall() {
        return shortfall;
    }
}
