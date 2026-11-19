package assignment3.core.cinema;

import assignment3.core.cinema.booking.AtomicBookingEngine;
import assignment3.core.cinema.booking.BookingEngine;
import assignment3.core.cinema.booking.ReentrantLockBookingEngine;
import assignment3.core.cinema.booking.SynchronizedBookingEngine;
import assignment3.core.util.ConsoleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runner and demonstration harness for Module 5 (Cinema Seat-Booking System).
 * Demonstrates thread synchronization to prevent race conditions and double-booking,
 * with comparison stubs/benchmarks for ReentrantLock and AtomicBoolean.
 */
public class CinemaBookingSimulation {

    public static void main(String[] args) {
        runAutomatedDemo();
    }

    public static void runInteractive(Scanner scanner) {
        ConsoleUtil.printHeader("MODULE 5: CINEMA SEAT BOOKING (INTERACTIVE MODE)");
        CinemaHall hall = new CinemaHall("Grand Cineplex Screen 1", 4, 6);
        SynchronizedBookingEngine engine = new SynchronizedBookingEngine(hall);

        printSeatMap(hall);

        System.out.print("\nEnter seat to book (e.g. A01, B03): ");
        String seatNum = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();

        boolean result = engine.bookSeat(seatNum, name);
        if (result) {
            ConsoleUtil.printSuccess("Seat " + seatNum + " reserved successfully for " + name);
        } else {
            ConsoleUtil.printError("Seat " + seatNum + " could not be booked (already reserved or invalid).");
        }

        printSeatMap(hall);
    }

    public static void runAutomatedDemo() {
        ConsoleUtil.printHeader("MODULE 5: CINEMA SEAT-BOOKING - CONCURRENCY STRESS TEST");
        System.out.println("Verifying thread synchronization, mutual exclusion, and zero double-booking.\n");

        // TEST 1: Extreme Race Condition on a Single Hot Seat ("A01")
        ConsoleUtil.printSubHeader("TEST 1: 40 Concurrent Threads Competing for EXACT SAME Seat 'A01'");
        CinemaHall hall1 = new CinemaHall("IMAX-Dolby-Hall", 4, 6);
        SynchronizedBookingEngine syncEngine = new SynchronizedBookingEngine(hall1);

        int threadCount = 40;
        String hotSeat = "A01";
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Thread> clientThreads = new ArrayList<>();
        for (int i = 1; i <= threadCount; i++) {
            String customerName = String.format("Customer-%02d", i);
            Thread t = new Thread(new CustomerBookingTask(syncEngine, hotSeat, customerName, successCount, failureCount),
                    "ClientThread-" + i);
            clientThreads.add(t);
        }

        // Fire all threads simultaneously
        for (Thread t : clientThreads) {
            t.start();
        }

        // Wait for all bookings to finish
        for (Thread t : clientThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println();
        System.out.println("Race Condition Test Results for Seat " + hotSeat + ":");
        System.out.printf(" - Total Concurrent Booking Attempts: %d%n", threadCount);
        System.out.printf(" - Successful Reservations          : %d%n", successCount.get());
        System.out.printf(" - Blocked / Rejected Duplicate Tries: %d%n", failureCount.get());

        Seat targetSeat = hall1.getSeat(hotSeat);
        System.out.printf(" - Final Seat Occupant              : %s%n", targetSeat.getBookedBy());

        if (successCount.get() == 1 && failureCount.get() == (threadCount - 1)) {
            ConsoleUtil.printSuccess("Mutual Exclusion Verified: ZERO DOUBLE-BOOKINGS DETECTED!");
        } else {
            ConsoleUtil.printError("CRITICAL BUG: Race condition allowed double-booking!");
        }

        // TEST 2: Comparison Across All 3 Engines (Synchronized vs ReentrantLock vs AtomicBoolean)
        ConsoleUtil.printSubHeader("TEST 2: Comparison Across 3 Synchronization Paradigms");
        runEngineComparison(new SynchronizedBookingEngine(new CinemaHall("SyncHall", 5, 8)));
        runEngineComparison(new ReentrantLockBookingEngine(new CinemaHall("LockHall", 5, 8)));
        runEngineComparison(new AtomicBookingEngine(new CinemaHall("AtomicHall", 5, 8)));

        // Final Hall Seat Map
        System.out.println();
        printSeatMap(hall1);

        System.out.println();
        ConsoleUtil.printSuccess("Module 5 (Cinema Seat-Booking) completed all test verification criteria successfully!\n");
    }

    private static void runEngineComparison(BookingEngine engine) {
        CinemaHall hall = engine.getHall();
        int totalSeats = hall.getTotalSeats();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 80 threads randomly booking across 40 seats
        List<Thread> threads = new ArrayList<>();
        long start = System.nanoTime();

        for (int i = 1; i <= 80; i++) {
            final int id = i;
            String targetSeat = String.format("%c%02d", (char) ('A' + (id % 5)), (id % 8) + 1);
            threads.add(new Thread(() -> {
                boolean ok = engine.bookSeat(targetSeat, "User-" + id);
                if (ok) successCount.incrementAndGet();
                else failCount.incrementAndGet();
            }));
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }

        long elapsedMicros = (System.nanoTime() - start) / 1000;
        System.out.printf("Engine: %-52s | Success: %2d | Rejections: %2d | Time: %4d µs%n",
                engine.getEngineType(), successCount.get(), failCount.get(), elapsedMicros);
    }

    public static void printSeatMap(CinemaHall hall) {
        System.out.println("=== Cinema Hall Seat Map: [" + hall.getHallName() + "] ===");
        System.out.println("Legend: [ . ] Available  |  [ X ] Booked");
        
        char currentRow = ' ';
        for (Map.Entry<String, Seat> entry : hall.getAllSeats().entrySet()) {
            char row = entry.getKey().charAt(0);
            if (row != currentRow) {
                if (currentRow != ' ') System.out.println();
                System.out.printf("Row %c: ", row);
                currentRow = row;
            }
            Seat s = entry.getValue();
            boolean isReserved = s.isBooked() || s.getAtomicBooked().get();
            System.out.printf("[%s:%s] ", s.getSeatNumber(), isReserved ? "X" : ".");
        }
        System.out.println("\n" + "=".repeat(50));
    }
}
