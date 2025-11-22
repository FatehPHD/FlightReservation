// File: datalayer/dao/SeatDAO.java
package datalayer.dao;

import businesslogic.entities.Seat;

import java.sql.SQLException;
import java.util.List;

public interface SeatDAO extends BaseDAO<Seat, Integer> {

    // All seats for a flight
    List<Seat> findByFlightId(long flightId) throws SQLException;

    // Only available seats for a flight
    List<Seat> findAvailableByFlight(long flightId) throws SQLException;

    // One seat by (flight, seat_number) pair
    Seat findByFlightAndSeatNumber(long flightId, String seatNumber) throws SQLException;

    // Just flip availability flag
    boolean updateAvailability(int seatId, boolean available) throws SQLException;
}
