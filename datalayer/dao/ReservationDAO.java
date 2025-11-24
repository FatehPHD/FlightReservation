package datalayer.dao;

import businesslogic.entities.Reservation;

import java.sql.SQLException;
import java.util.List;

public interface ReservationDAO extends BaseDAO<Reservation, Integer> {
    
    /**
     * Find all reservations for a specific customer.
     * @param customerId Customer ID
     * @return List of reservations for the customer
     * @throws SQLException if database error occurs
     */
    List<Reservation> findByCustomerId(Integer customerId) throws SQLException;
    
    /**
     * Find all reservations for a specific flight.
     * @param flightId Flight ID
     * @return List of reservations for the flight
     * @throws SQLException if database error occurs
     */
    List<Reservation> findByFlightId(Integer flightId) throws SQLException;
}
