package assignment3.core.cinema;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Domain entity representing an individual cinema seat.
 * Contains synchronization locks and atomic state flags for concurrent evaluation.
 */
public class Seat {
    private final String seatNumber;
    private final String category;
    private final double price;

    // State for synchronized-based booking
    private boolean booked = false;
    private String bookedBy = null;

    // Lock for ReentrantLock comparison engine
    private final ReentrantLock lock = new ReentrantLock(true); // Fair lock

    // Atomic boolean for lock-free CAS comparison engine
    private final AtomicBoolean atomicBooked = new AtomicBoolean(false);
    private volatile String atomicBookedBy = null;

    public Seat(String seatNumber, String category, double price) {
        this.seatNumber = seatNumber;
        this.category = category;
        this.price = price;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public AtomicBoolean getAtomicBooked() {
        return atomicBooked;
    }

    public String getAtomicBookedBy() {
        return atomicBookedBy;
    }

    public void setAtomicBookedBy(String atomicBookedBy) {
        this.atomicBookedBy = atomicBookedBy;
    }

    public void reset() {
        this.booked = false;
        this.bookedBy = null;
        this.atomicBooked.set(false);
        this.atomicBookedBy = null;
    }

    @Override
    public String toString() {
        return String.format("[%s (%s) - $%.2f | Status: %s]",
                seatNumber, category, price, (booked || atomicBooked.get()) ? "BOOKED" : "AVAILABLE");
    }
}
