package assignment3.core.cinema.booking;

import assignment3.core.cinema.CinemaHall;
import assignment3.core.cinema.Seat;
import assignment3.core.util.ConsoleUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Comparison implementation utilizing explicit java.util.concurrent.locks.ReentrantLock.
 * Demonstrates timed tryLock(), fair lock acquisition, and explicit unlock() in finally.
 */
public class ReentrantLockBookingEngine implements BookingEngine {

    private final CinemaHall hall;

    public ReentrantLockBookingEngine(CinemaHall hall) {
        this.hall = hall;
    }

    @Override
    public boolean bookSeat(String seatNumber, String customerName) {
        Seat seat = hall.getSeat(seatNumber);
        if (seat == null) {
            return false;
        }

        ReentrantLock seatLock = seat.getLock();
        boolean acquired = false;

        try {
            // Attempt to acquire lock with timeout (e.g. 50ms) to prevent thread starvation
            acquired = seatLock.tryLock(50, TimeUnit.MILLISECONDS);
            if (!acquired) {
                // Could not acquire lock within timeout window
                return false;
            }

            // CRITICAL SECTION
            if (seat.isBooked()) {
                return false;
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            seat.setBooked(true);
            seat.setBookedBy(customerName);
            return true;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;

        } finally {
            // ALWAYS release lock in finally block if acquired
            if (acquired) {
                seatLock.unlock();
            }
        }
    }

    @Override
    public boolean cancelSeat(String seatNumber, String customerName) {
        Seat seat = hall.getSeat(seatNumber);
        if (seat == null) return false;

        ReentrantLock seatLock = seat.getLock();
        seatLock.lock();
        try {
            if (seat.isBooked() && customerName.equals(seat.getBookedBy())) {
                seat.setBooked(false);
                seat.setBookedBy(null);
                return true;
            }
            return false;
        } finally {
            seatLock.unlock();
        }
    }

    @Override
    public CinemaHall getHall() {
        return hall;
    }

    @Override
    public String getEngineType() {
        return "Explicit ReentrantLock (Fair & Timed tryLock)";
    }
}
