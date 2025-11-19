package datalayer.dao;

import businesslogic.entities.Airline;

public interface AirlineDAO extends BaseDAO<Airline, Integer> {
    // Add airline-specific query methods here if needed later
    // e.g., List<Airline> findByCountry(String country) throws SQLException;
}
