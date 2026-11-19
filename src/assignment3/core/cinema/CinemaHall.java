package assignment3.core.cinema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a cinema auditorium containing an inventory of seats.
 */
public class CinemaHall {
    private final String hallName;
    private final Map<String, Seat> seats = new LinkedHashMap<>();

    public CinemaHall(String hallName, int rows, int seatsPerRow) {
        this.hallName = hallName;
        initializeSeats(rows, seatsPerRow);
    }

    private void initializeSeats(int rows, int seatsPerRow) {
        char rowLetter = 'A';
        for (int r = 0; r < rows; r++) {
            String category = (r == 0) ? "VIP" : (r < 3 ? "PREMIUM" : "STANDARD");
            double price = (r == 0) ? 25.0 : (r < 3 ? 18.0 : 12.0);

            for (int s = 1; s <= seatsPerRow; s++) {
                String seatNum = String.format("%c%02d", rowLetter, s);
                seats.put(seatNum, new Seat(seatNum, category, price));
            }
            rowLetter++;
        }
    }

    public Seat getSeat(String seatNumber) {
        return seats.get(seatNumber);
    }

    public Map<String, Seat> getAllSeats() {
        return Collections.unmodifiableMap(seats);
    }

    public int getTotalSeats() {
        return seats.size();
    }

    public void resetAllSeats() {
        seats.values().forEach(Seat::reset);
    }

    public String getHallName() {
        return hallName;
    }
}
