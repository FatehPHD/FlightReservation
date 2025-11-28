package datalayer.dao;

import businesslogic.entities.Route;

import java.sql.SQLException;
import java.util.List;

public interface RouteDAO extends BaseDAO<Route, Integer> {
    /**
     * Find all routes where the airport is either the origin or destination.
     * @param airportCode Airport code
     * @return List of routes using this airport
     * @throws SQLException if database error occurs
     */
    List<Route> findByAirportCode(String airportCode) throws SQLException;
}
