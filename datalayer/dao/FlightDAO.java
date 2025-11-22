// File: datalayer/dao/FlightDAO.java
package datalayer.dao;

import businesslogic.entities.Flight;
import businesslogic.entities.enums.FlightStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface FlightDAO extends BaseDAO<Flight, Integer> {

    Flight findByFlightNumber(String flightNumber) throws SQLException;

    List<Flight> findByStatus(FlightStatus status) throws SQLException;

    List<Flight> findByRoute(String originCode, String destinationCode) throws SQLException;

    List<Flight> findByDepartureRange(LocalDateTime from, LocalDateTime to) throws SQLException;
}
