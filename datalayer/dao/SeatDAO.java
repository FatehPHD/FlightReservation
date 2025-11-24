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
}
