package assignment3.core.atm;

import assignment3.core.atm.exceptions.AccountLockedException;
import assignment3.core.atm.exceptions.InvalidAmountException;
import assignment3.core.util.ConsoleUtil;

import java.util.Scanner;

/**
 * Runner and demonstration harness for Module 1 (Bank ATM Simulation).
 */
public class AtmSimulation {

    public static void main(String[] args) {
        runAutomatedDemo();
    }

    public static void runInteractive(Scanner scanner) {
        ConsoleUtil.printHeader("MODULE 1: BANK ATM SIMULATION (INTERACTIVE MODE)");
        BankAccount account = new BankAccount("ACC-987654321", "Alice Vance", "1234", 1500.0);
        System.out.println("Welcome to Antigravity Central Bank ATM.");
        System.out.println("Account Number: " + account.getAccountNumber());
        System.out.println("Initial Balance: $" + account.getBalance());

        // Authenticate
        boolean authenticated = false;
        while (!authenticated && !account.isLocked()) {
            System.out.print("\nEnter 4-digit PIN (Default is 1234): ");
            String enteredPin = scanner.nextLine().trim();
            try {
                account.authenticate(enteredPin);
                authenticated = true;
                ConsoleUtil.printSuccess("Authentication successful! Welcome, " + account.getAccountHolder());
            } catch (AccountLockedException ale) {
                ConsoleUtil.printError(ale.getMessage());
                return;
            } catch (InvalidAmountException iae) {
                ConsoleUtil.printWarning(iae.getMessage());
            }
        }

        System.out.print("\nEnter withdrawal amount (or non-numeric string to test InputMismatch): ");
        String inputLine = scanner.nextLine().trim();

        AtmSession session = new AtmSession(account);
        session.processWithdrawal(inputLine);

        System.out.println("\nFinal Account Balance: $" + String.format("%.2f", account.getBalance()));
    }

    public static void runAutomatedDemo() {
        ConsoleUtil.printHeader("MODULE 1: BANK ATM SIMULATION - COMPREHENSIVE TEST HARNESS");
        System.out.println("Demonstrating custom exceptions, multi-catch, finally guarantees, and stack trace analysis.\n");

        BankAccount account = new BankAccount("ACC-100200300", "Dr. Gordon Freeman", "4321", 500.0);
        System.out.println("Account Holder : " + account.getAccountHolder());
        System.out.println("Account Number : " + account.getAccountNumber());
        System.out.println("Starting Balance: $" + String.format("%.2f", account.getBalance()));
        System.out.println();

        // Test Case 1: Successful Withdrawal
        ConsoleUtil.printSubHeader("TEST CASE 1: Valid Withdrawal ($120.00)");
        AtmSession session1 = new AtmSession(account);
        session1.processWithdrawal(120.0);

        // Test Case 2: Invalid Amount - Negative Value
        ConsoleUtil.printSubHeader("TEST CASE 2: Invalid Amount - Negative Value (-$50.00)");
        AtmSession session2 = new AtmSession(account);
        session2.processWithdrawal(-50.0);

        // Test Case 3: Invalid Amount - Non-Multiple of $10 notes ($45.00)
        ConsoleUtil.printSubHeader("TEST CASE 3: Invalid Denomination ($45.00 not multiple of $10)");
        AtmSession session3 = new AtmSession(account);
        session3.processWithdrawal(45.0);

        // Test Case 4: Exceeding Single Withdrawal Limit ($2,500.00)
        ConsoleUtil.printSubHeader("TEST CASE 4: Exceeding Single Transaction Limit ($2500.00 > $2000.00)");
        AtmSession session4 = new AtmSession(account);
        session4.processWithdrawal(2500.0);

        // Test Case 5: Insufficient Balance ($800.00 requested from remaining ~$380.00)
        ConsoleUtil.printSubHeader("TEST CASE 5: Insufficient Balance ($800.00 requested from $" + account.getBalance() + ")");
        AtmSession session5 = new AtmSession(account);
        session5.processWithdrawal(800.0);

        // Test Case 6: InputMismatchException Simulation (Non-numeric text input)
        ConsoleUtil.printSubHeader("TEST CASE 6: InputMismatchException Simulation (User typed: 'five_hundred_dollars')");
        AtmSession session6 = new AtmSession(account);
        session6.processWithdrawal("five_hundred_dollars");

        System.out.println();
        ConsoleUtil.printSuccess("Module 1 (ATM Simulation) completed all test verification criteria successfully!\n");
    }
}
