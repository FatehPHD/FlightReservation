package datalayer.dao;

import businesslogic.entities.User;
import businesslogic.entities.Customer;
import businesslogic.entities.FlightAgent;
import businesslogic.entities.SystemAdmin;

import java.sql.SQLException;
import java.util.List;

public interface UserDAO extends BaseDAO<User, Integer> {

    User findByUsername(String username) throws SQLException;

    List<Customer> findAllCustomers() throws SQLException;

    List<FlightAgent> findAllFlightAgents() throws SQLException;

    List<SystemAdmin> findAllSystemAdmins() throws SQLException;
}
