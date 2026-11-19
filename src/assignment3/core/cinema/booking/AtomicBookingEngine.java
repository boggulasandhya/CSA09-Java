package assignment3.core.cinema.booking;

import assignment3.core.cinema.CinemaHall;
import assignment3.core.cinema.Seat;

/**
 * Comparison implementation utilizing lock-free Compare-And-Swap (CAS)
 * primitives via java.util.concurrent.atomic.AtomicBoolean.
 */
public class AtomicBookingEngine implements BookingEngine {

    private final CinemaHall hall;

    public AtomicBookingEngine(CinemaHall hall) {
        this.hall = hall;
    }

    @Override
    public boolean bookSeat(String seatNumber, String customerName) {
        Seat seat = hall.getSeat(seatNumber);
        if (seat == null) return false;

        // LOCK-FREE ATOMIC CAS:
        // compareAndSet atomically checks if current value is false,
        // and if so sets it to true in a single non-blocking CPU instruction.
        boolean acquired = seat.getAtomicBooked().compareAndSet(false, true);
        if (acquired) {
            seat.setAtomicBookedBy(customerName);
            return true;
        }
        return false;
    }

    @Override
    public boolean cancelSeat(String seatNumber, String customerName) {
        Seat seat = hall.getSeat(seatNumber);
        if (seat == null) return false;

        if (customerName.equals(seat.getAtomicBookedBy())) {
            seat.setAtomicBookedBy(null);
            seat.getAtomicBooked().set(false);
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
        return "AtomicBoolean (Lock-Free Hardware CAS compareAndSet)";
    }
}
