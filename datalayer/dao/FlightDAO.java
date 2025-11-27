package datalayer.dao;

import businesslogic.entities.Flight;

import java.sql.SQLException;
import java.util.List;

public interface FlightDAO extends BaseDAO<Flight, Integer> {
    
    /**
     * Find flight by flight number (e.g., "AC123").
     * @param flightNumber Flight number
     * @return Flight or null if not found
     * @throws SQLException if database error occurs
     */
    Flight findByFlightNumber(String flightNumber) throws SQLException;
    
    /**
     * Find all flights for a specific aircraft.
     * @param aircraftId Aircraft ID
     * @return List of flights using this aircraft
     * @throws SQLException if database error occurs
     */
    List<Flight> findByAircraftId(Integer aircraftId) throws SQLException;
}
