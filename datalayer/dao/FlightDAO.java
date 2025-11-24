package datalayer.dao;

import businesslogic.entities.Flight;

import java.sql.SQLException;

public interface FlightDAO extends BaseDAO<Flight, Integer> {
    
    /**
     * Find flight by flight number (e.g., "AC123").
     * @param flightNumber Flight number
     * @return Flight or null if not found
     * @throws SQLException if database error occurs
     */
    Flight findByFlightNumber(String flightNumber) throws SQLException;
}
