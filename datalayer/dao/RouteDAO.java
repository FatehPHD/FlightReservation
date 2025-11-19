package datalayer.dao;

import businesslogic.entities.Route;

public interface RouteDAO extends BaseDAO<Route, Integer> {
    // Add route-specific queries here if needed
    // e.g., List<Route> findByAirports(String originCode, String destinationCode) throws SQLException;
}
