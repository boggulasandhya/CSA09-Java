package assignment3.core;

import assignment3.core.atm.AtmSimulation;
import assignment3.core.cinema.CinemaBookingSimulation;
import assignment3.core.exam.ExamPortalSimulation;
import assignment3.core.results.SemesterResultSimulation;
import assignment3.core.sales.SalesReportSimulation;
import assignment3.core.scanner.ScannerSimulation;
import assignment3.core.util.ConsoleUtil;
import assignment3.core.wallet.WalletSimulation;

import java.util.Scanner;

/**
 * Main entry point for the Assignment 3 Java Console Application Suite.
 * Features an interactive menu and CLI arguments for executing any of the 7 modules
 * or running the full automated verification test harness.
 */
public class MainApplication {

    public static void main(String[] args) {
        if (args.length > 0) {
            handleCliArguments(args);
            return;
        }

        runInteractiveMenu();
    }

    private static void handleCliArguments(String[] args) {
        String command = args[0].trim().toLowerCase();
        switch (command) {
            case "--all", "-a" -> runAllAutomated();
            case "--module", "-m" -> {
                if (args.length < 2) {
                    System.out.println("Please specify a module number (1-7).");
                    return;
                }
                executeModule(args[1].trim());
            }
            case "--help", "-h" -> printHelp();
            default -> {
                System.out.println("Unrecognized argument: " + command);
                printHelp();
            }
        }
    }

    private static void printHelp() {
        System.out.println("Assignment 3 Application Suite - CLI Usage:");
        System.out.println("  java assignment3.core.MainApplication              (Interactive Menu)");
        System.out.println("  java assignment3.core.MainApplication --all        (Run all 7 modules automated)");
        System.out.println("  java assignment3.core.MainApplication -m <1..7>    (Run specific module automated)");
        System.out.println("  java assignment3.core.MainApplication --help       (Display this help)");
    }

    public static void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            ConsoleUtil.printHeader("ADVANCED EXCEPTION HANDLING & MULTITHREADING SUITE");
            System.out.println("Select a module to execute:");
            System.out.println("  [1] Q1: Bank ATM Simulation (Multi-Catch, Finally, Stack Trace)");
            System.out.println("  [2] Q2: Online Examination Portal (3-Tier Layered Exception Chaining)");
            System.out.println("  [3] Q3: Daily Sales Report (Try-With-Resources, Suppressed Exceptions, Leaks)");
            System.out.println("  [4] Q4: Parallel Answer-Script Scanning (Thread vs Runnable, Daemon, Priorities)");
            System.out.println("  [5] Q5: Cinema Seat-Booking (Synchronized Blocks, ReentrantLock, AtomicBoolean)");
            System.out.println("  [6] Q6: Hostel Wallet & Mess Token (Deadlock Mitigation, Bounded Buffer wait/notify)");
            System.out.println("  [7] Q7: Semester Result Processor (ExecutorService, Callable, Future - 10,000 files)");
            System.out.println("  [8] Run ALL Modules (Complete Automated Showcase Test Harness)");
            System.out.println("  [9] Exit");
            System.out.print(ConsoleUtil.YELLOW + "\nEnter choice (1-9): " + ConsoleUtil.RESET);

            String input = scanner.nextLine().trim();
            System.out.println();

            switch (input) {
                case "1" -> AtmSimulation.runInteractive(scanner);
                case "2" -> ExamPortalSimulation.runInteractive(scanner);
                case "3" -> SalesReportSimulation.runInteractive(scanner);
                case "4" -> ScannerSimulation.runInteractive(scanner);
                case "5" -> CinemaBookingSimulation.runInteractive(scanner);
                case "6" -> WalletSimulation.runInteractive(scanner);
                case "7" -> SemesterResultSimulation.runInteractive(scanner);
                case "8" -> runAllAutomated();
                case "9", "exit", "q" -> {
                    System.out.println("Exiting application suite. Goodbye!");
                    running = false;
                }
                default -> ConsoleUtil.printError("Invalid selection. Please choose an option between 1 and 9.");
            }

            if (running && !input.equals("8")) {
                System.out.println(ConsoleUtil.WHITE + "\nPress Enter to return to main menu..." + ConsoleUtil.RESET);
                scanner.nextLine();
            }
        }
    }

    private static void executeModule(String moduleNum) {
        switch (moduleNum) {
            case "1" -> AtmSimulation.runAutomatedDemo();
            case "2" -> ExamPortalSimulation.runAutomatedDemo();
            case "3" -> SalesReportSimulation.runAutomatedDemo();
            case "4" -> ScannerSimulation.runAutomatedDemo();
            case "5" -> CinemaBookingSimulation.runAutomatedDemo();
            case "6" -> WalletSimulation.runAutomatedDemo();
            case "7" -> SemesterResultSimulation.runAutomatedDemo();
            default -> System.out.println("Invalid module number: " + moduleNum);
        }
    }

    public static void runAllAutomated() {
        ConsoleUtil.printHeader("STARTING COMPLETE 7-MODULE AUTOMATED SHOWCASE");
        ConsoleUtil.sleep(300);

        AtmSimulation.runAutomatedDemo();
        ConsoleUtil.sleep(400);

        ExamPortalSimulation.runAutomatedDemo();
        ConsoleUtil.sleep(400);

        SalesReportSimulation.runAutomatedDemo();
        ConsoleUtil.sleep(400);

        ScannerSimulation.runAutomatedDemo();
        ConsoleUtil.sleep(400);

        CinemaBookingSimulation.runAutomatedDemo();
        ConsoleUtil.sleep(400);

        WalletSimulation.runAutomatedDemo();
        ConsoleUtil.sleep(400);

        SemesterResultSimulation.runAutomatedDemo();

        ConsoleUtil.printHeader("ALL 7 MODULES EXECUTED AND VERIFIED SUCCESSFULLY!");
    }
}
