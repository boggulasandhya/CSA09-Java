package assignment3.core.cinema.booking;

import assignment3.core.cinema.CinemaHall;
import assignment3.core.cinema.Seat;
import assignment3.core.util.ConsoleUtil;

/**
 * Production implementation of cinema booking engine utilizing Java intrinsic monitor locks:
 * synchronized methods and fine-grained synchronized blocks to prevent double-booking.
 */
public class SynchronizedBookingEngine implements BookingEngine {

    private final CinemaHall hall;

    public SynchronizedBookingEngine(CinemaHall hall) {
        this.hall = hall;
    }

    /**
     * Books a seat using a fine-grained SYNCHRONIZED BLOCK on the target Seat monitor.
     * Fine-grained locking ensures that concurrent bookings for DIFFERENT seats
     * proceed in parallel without blocking each other, maximizing throughput.
     */
    @Override
    public boolean bookSeat(String seatNumber, String customerName) {
        Seat seat = hall.getSeat(seatNumber);
        if (seat == null) {
            ConsoleUtil.printError("Seat " + seatNumber + " does not exist in hall " + hall.getHallName());
            return false;
        }

        // SYNCHRONIZED BLOCK: Lock ONLY on this specific Seat instance!
        synchronized (seat) {
            // CRITICAL SECTION START
            if (seat.isBooked()) {
                // Already booked by another concurrent thread
                return false;
            }

            // Simulate minor processing delay within critical section
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            seat.setBooked(true);
            seat.setBookedBy(customerName);
            return true;
            // CRITICAL SECTION END (Monitor automatically released upon exit)
        }
    }

    /**
     * Demonstrates SYNCHRONIZED METHOD on the engine for hall-level admin actions.
     */
    @Override
    public synchronized boolean cancelSeat(String seatNumber, String customerName) {
        Seat seat = hall.getSeat(seatNumber);
        if (seat == null || !seat.isBooked()) {
            return false;
        }

        if (customerName.equals(seat.getBookedBy())) {
            seat.setBooked(false);
            seat.setBookedBy(null);
            return true;
        }
        return false;
    }

    @Override
    public CinemaHall getHall() {
        return hall;
    }

    @Override
    public String getEngineType() {
        return "Synchronized (Intrinsic Monitors & Fine-Grained Blocks)";
    }
}
