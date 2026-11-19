package assignment3.core.cinema.booking;

import assignment3.core.cinema.CinemaHall;

/**
 * Common contract for cinema seat reservation engines.
 */
public interface BookingEngine {

    /**
     * Attempts to book the specified seat for a customer.
     *
     * @param seatNumber Target seat identifier (e.g., "A01")
     * @param customerName Customer name or thread ID
     * @return true if booking succeeded, false if seat was already reserved
     */
    boolean bookSeat(String seatNumber, String customerName);

    /**
     * Cancels an existing booking.
     */
    boolean cancelSeat(String seatNumber, String customerName);

    /**
     * Returns the cinema hall managed by this engine.
     */
    CinemaHall getHall();

    /**
     * Returns the name/pattern of this synchronization engine.
     */
    String getEngineType();
}
