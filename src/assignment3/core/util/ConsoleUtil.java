package assignment3.core.util;

/**
 * Utility class providing ANSI styling, headers, and formatted logging
 * for the Assignment 3 console application suite.
 */
public final class ConsoleUtil {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    private ConsoleUtil() {
        // Prevent instantiation
    }

    public static void printHeader(String title) {
        String bar = "=".repeat(75);
        System.out.println(CYAN + BOLD + bar + RESET);
        System.out.println(CYAN + BOLD + "   " + title + RESET);
        System.out.println(CYAN + BOLD + bar + RESET);
    }

    public static void printSubHeader(String subTitle) {
        String bar = "-".repeat(60);
        System.out.println(BLUE + bar + RESET);
        System.out.println(BLUE + BOLD + ">> " + subTitle + RESET);
        System.out.println(BLUE + bar + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "[SUCCESS] " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(CYAN + "[INFO]    " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "[WARNING] " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "[ERROR]   " + message + RESET);
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
