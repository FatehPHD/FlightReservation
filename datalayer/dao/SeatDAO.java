package datalayer.dao;

import businesslogic.entities.Seat;
import businesslogic.entities.enums.SeatClass;

import java.sql.SQLException;
import java.util.List;

public interface SeatDAO extends BaseDAO<Seat, Integer> {
    
    /**
     * Find all seats for a specific flight.
     * @param flightId Flight ID
     * @return List of seats for the flight
     * @throws SQLException if database error occurs
     */
    List<Seat> findByFlightId(Integer flightId) throws SQLException;
    
    /**
     * Find available seats for a specific flight.
     * @param flightId Flight ID
     * @return List of available seats for the flight
     * @throws SQLException if database error occurs
     */
    List<Seat> findAvailableSeatsByFlightId(Integer flightId) throws SQLException;
    
    /**
     * Find seats by flight ID and seat class.
     * @param flightId Flight ID
     * @param seatClass Seat class (ECONOMY, BUSINESS, FIRST)
     * @return List of seats matching the criteria
     * @throws SQLException if database error occurs
     */
    List<Seat> findByFlightIdAndSeatClass(Integer flightId, SeatClass seatClass) throws SQLException;

    /**
     * Create seats for a specific flight.
     * @param flightId Flight ID
     * @param totalSeats Total seats to create (must match aircraft's total seats)
     * @param seatConfiguration Seat configuration (e.g., "3-3", "2-4-2")
     * @param availableSeats Number of seats to mark as available (must be <= totalSeats)
     * @throws SQLException if database error occurs
     */
    void createSeatsForFlight(int flightId, int totalSeats, String seatConfiguration, int availableSeats) throws SQLException;
    
    /**
     * Update seat availability for a flight based on available seats count.
     * Marks the first N seats as available, rest as unavailable.
     * @param flightId Flight ID
     * @param availableSeats Number of seats to mark as available
     * @throws SQLException if database error occurs
     */
    void updateSeatAvailability(int flightId, int availableSeats) throws SQLException;
}
