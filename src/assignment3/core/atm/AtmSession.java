package assignment3.core.atm;

import assignment3.core.atm.exceptions.AccountLockedException;
import assignment3.core.atm.exceptions.AtmException;
import assignment3.core.atm.exceptions.InsufficientBalanceException;
import assignment3.core.atm.exceptions.InvalidAmountException;
import assignment3.core.util.ConsoleUtil;

import java.util.InputMismatchException;
import java.util.UUID;

/**
 * Manages an ATM user session with hardware lifecycle guarantees,
 * multi-catch exception handling, finally cleanup, and stack trace inspection.
 */
public class AtmSession {

    private final String sessionId;
    private final BankAccount account;
    private boolean cashDispenserShutterOpen = false;
    private boolean sessionActive = false;
    private boolean cardRetained = false;

    public AtmSession(BankAccount account) {
        this.sessionId = UUID.randomUUID().toString().substring(0, 8);
        this.account = account;
        this.sessionActive = true;
    }

    /**
     * Executes withdrawal transaction with multi-catch blocks,
     * finally cleanup, and stack trace inspection.
     *
     * @param amountInput Object representing user input (Number or raw String for testing InputMismatch)
     */
    public boolean processWithdrawal(Object amountInput) {
        System.out.println(ConsoleUtil.MAGENTA + "[AtmSession:" + sessionId + "] Starting withdrawal workflow for account: " 
                + account.getAccountNumber() + ConsoleUtil.RESET);

        boolean transactionSuccess = false;

        try {
            // Simulate hardware initialization
            this.cashDispenserShutterOpen = false;

            double parsedAmount;

            // Robust parsing simulating Scanner.nextDouble()
            if (amountInput instanceof Number) {
                parsedAmount = ((Number) amountInput).doubleValue();
            } else if (amountInput instanceof String) {
                try {
                    parsedAmount = Double.parseDouble((String) amountInput);
                } catch (NumberFormatException nfe) {
                    // Mimic Scanner.nextDouble() throwing InputMismatchException on invalid token
                    InputMismatchException ime = new InputMismatchException(
                            "InputMismatchException: Non-numeric token '" + amountInput + "' cannot be parsed as double currency.");
                    ime.initCause(nfe);
                    throw ime;
                }
            } else {
                throw new InputMismatchException("Invalid input type passed to ATM cash processor: " + amountInput);
            }

            System.out.println("-> Validating withdrawal request: $" + String.format("%.2f", parsedAmount));

            // Business logic execution (throws custom checked exceptions)
            account.withdraw(parsedAmount);

            // Hardware action: Dispense cash
            this.cashDispenserShutterOpen = true;
            System.out.println(ConsoleUtil.GREEN + "-> [CASH DISPENSER] Shutter OPENED. Dispensed: $" 
                    + String.format("%.2f", parsedAmount) + ConsoleUtil.RESET);

            transactionSuccess = true;
            ConsoleUtil.printSuccess("Transaction approved! New balance: $" + String.format("%.2f", account.getBalance()));

        } catch (InsufficientBalanceException | InvalidAmountException ex) {
            // MULTI-CATCH BLOCK: Catches both custom ATM domain exceptions
            ConsoleUtil.printError("Domain Exception Caught via Multi-Catch Block: " + ex.getClass().getSimpleName());
            System.out.println(ConsoleUtil.RED + "   Reason: " + ex.getMessage() + ConsoleUtil.RESET);
            
            // Perform Stack Trace Analysis
            analyzeStackTrace(ex);

        } catch (InputMismatchException ex) {
            // SEPARATE CATCH BLOCK: Handles malformed/non-numeric input
            ConsoleUtil.printError("Input Parsing Exception: Invalid numeric format provided.");
            System.out.println(ConsoleUtil.YELLOW + "   Details: " + ex.getMessage() + ConsoleUtil.RESET);
            analyzeStackTrace(ex);

        } catch (Exception ex) {
            // GENERAL CATCH BLOCK: Handles unexpected system crashes
            ConsoleUtil.printError("Unexpected fatal error in ATM controller: " + ex.getMessage());
            analyzeStackTrace(ex);

        } finally {
            // FINALLY CLEANUP: Always runs regardless of normal return or uncaught exception
            performHardwareCleanup();
        }

        return transactionSuccess;
    }

    /**
     * Performs detailed programmatic stack trace analysis.
     */
    public static void analyzeStackTrace(Throwable throwable) {
        System.out.println(ConsoleUtil.BLUE + "   --- [Stack Trace Analysis] ---" + ConsoleUtil.RESET);
        System.out.println(ConsoleUtil.BLUE + "   Exception Type: " + throwable.getClass().getName() + ConsoleUtil.RESET);
        System.out.println(ConsoleUtil.BLUE + "   Message:        " + throwable.getMessage() + ConsoleUtil.RESET);
        
        StackTraceElement[] elements = throwable.getStackTrace();
        if (elements != null && elements.length > 0) {
            System.out.println(ConsoleUtil.BLUE + "   Origin Frame:   " 
                    + elements[0].getClassName() + "." + elements[0].getMethodName() 
                    + " (" + elements[0].getFileName() + ":" + elements[0].getLineNumber() + ")" + ConsoleUtil.RESET);
            
            System.out.println("   Call Stack Depth: " + elements.length + " frames");
            int framesToPrint = Math.min(3, elements.length);
            for (int i = 0; i < framesToPrint; i++) {
                System.out.println("     [" + i + "] " + elements[i].toString());
            }
        }

        if (throwable.getCause() != null) {
            System.out.println(ConsoleUtil.YELLOW + "   Underlying Root Cause: " 
                    + throwable.getCause().getClass().getName() + " -> " + throwable.getCause().getMessage() + ConsoleUtil.RESET);
        }
        System.out.println(ConsoleUtil.BLUE + "   ------------------------------" + ConsoleUtil.RESET);
    }

    /**
     * Critical finally block routine to reset hardware locks and guarantee security state.
     */
    private void performHardwareCleanup() {
        System.out.println(ConsoleUtil.CYAN + "-> [FINALLY BLOCK EXECUTING] Initiating guaranteed ATM hardware cleanup..." + ConsoleUtil.RESET);
        
        if (cashDispenserShutterOpen) {
            System.out.println("   [HARDWARE] Closing cash dispenser shutter door.");
            cashDispenserShutterOpen = false;
        }

        System.out.println("   [SECURITY] Purging temporary session credentials and PIN buffer from cache.");
        System.out.println("   [AUDIT]    Recording session transaction audit entry for SessionId: " + sessionId);
        this.sessionActive = false;
        System.out.println(ConsoleUtil.GREEN + "-> [FINALLY BLOCK FINISHED] ATM hardware returned to idle secure state." + ConsoleUtil.RESET);
    }

    public boolean isSessionActive() {
        return sessionActive;
    }

    public String getSessionId() {
        return sessionId;
    }
}
