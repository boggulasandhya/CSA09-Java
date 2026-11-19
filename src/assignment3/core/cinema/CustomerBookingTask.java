package assignment3.core.cinema;

import assignment3.core.cinema.booking.BookingEngine;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runnable task simulating an individual customer's concurrent seat booking attempt.
 */
public class CustomerBookingTask implements Runnable {

    private final BookingEngine engine;
    private final String seatNumber;
    private final String customerName;
    private final AtomicInteger successCounter;
    private final AtomicInteger failureCounter;

    public CustomerBookingTask(BookingEngine engine, String seatNumber, String customerName,
                               AtomicInteger successCounter, AtomicInteger failureCounter) {
        this.engine = engine;
        this.seatNumber = seatNumber;
        this.customerName = customerName;
        this.successCounter = successCounter;
        this.failureCounter = failureCounter;
    }

    @Override
    public void run() {
        boolean booked = engine.bookSeat(seatNumber, customerName);
        if (booked) {
            successCounter.incrementAndGet();
            System.out.printf("   [SUCCESS] Customer '%s' successfully booked seat %s!%n",
                    customerName, seatNumber);
        } else {
            failureCounter.incrementAndGet();
        }
    }
}
