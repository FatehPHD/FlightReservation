// File: datalayer/dao/ReservationDAO.java
package datalayer.dao;

import businesslogic.entities.Reservation;
import businesslogic.entities.enums.ReservationStatus;

import java.sql.SQLException;
import java.util.List;

public interface ReservationDAO extends BaseDAO<Reservation, Integer> {

    // Extra query methods specific to Reservation

    List<Reservation> findByCustomerId(int customerId) throws SQLException;

    List<Reservation> findByFlightId(int flightId) throws SQLException;

    List<Reservation> findByStatus(ReservationStatus status) throws SQLException;
}
