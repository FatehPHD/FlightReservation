package tests;

import businesslogic.entities.Flight;
import businesslogic.entities.Aircraft;
import businesslogic.entities.Route;
import businesslogic.entities.Airport;
import businesslogic.entities.enums.FlightStatus;
import datalayer.dao.FlightDAO;
import datalayer.impl.FlightDAOImpl;
import datalayer.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TestFlightDAO {

    public static void main(String[] args) {
        try {
            // Ensure dependencies exist
            ensureTestDependenciesExist();

            FlightDAO flightDAO = new FlightDAOImpl();

            System.out.println("==== TEST: SAVE ====");
            Flight flight = new Flight();
            flight.setFlightNumber("AC123");
            flight.setDepartureTime(LocalDateTime.now().plusDays(1));
            flight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(3));
            flight.setStatus(FlightStatus.SCHEDULED);
            flight.setAvailableSeats(150);
            flight.setPrice(299.99);

            // Set aircraft
            Aircraft aircraft = new Aircraft();
            aircraft.setAircraftId(getTestAircraftId());
            flight.setAircraft(aircraft);

            // Set route
            Route route = new Route();
            route.setRouteId(getTestRouteId());
            Airport origin = new Airport();
            origin.setAirportCode("YYC");
            Airport dest = new Airport();
            dest.setAirportCode("YYZ");
            route.setOrigin(origin);
            route.setDestination(dest);
            flight.setRoute(route);

            Flight saved = flightDAO.save(flight);
            System.out.println("Saved flight: " + saved.getFlightNumber());
            System.out.println("Flight: " + saved);

            System.out.println("\n==== TEST: FIND BY FLIGHT NUMBER ====");
            Flight found = flightDAO.findByFlightNumber("AC123");
            System.out.println("Found by flight number: " + found);

            System.out.println("\n==== TEST: FIND BY ID ====");
            // Get flight_id from database
            Integer flightId = getFlightIdByNumber("AC123");
            if (flightId != null) {
                Flight foundById = flightDAO.findById(flightId);
                System.out.println("Found by ID: " + foundById);
            }

            System.out.println("\n==== TEST: UPDATE ====");
            found.setStatus(FlightStatus.DELAYED);
            found.setAvailableSeats(140);
            found.setPrice(349.99);
            boolean updated = flightDAO.update(found);
            System.out.println("Update result: " + updated);

            Flight updatedFromDb = flightDAO.findByFlightNumber("AC123");
            System.out.println("Updated flight from DB: " + updatedFromDb);

            System.out.println("\n==== TEST: FIND ALL ====");
            List<Flight> all = flightDAO.findAll();
            System.out.println("Total flights in DB: " + all.size());
            for (Flight f : all) {
                System.out.println(f);
            }

            System.out.println("\n==== TEST: DELETE ====");
            if (flightId != null) {
                boolean deleted = flightDAO.delete(flightId);
                System.out.println("Delete result: " + deleted);

                Flight afterDelete = flightDAO.findById(flightId);
                System.out.println("Find after delete (should be null): " + afterDelete);
            }

            System.out.println("\n==== TESTS COMPLETED ====");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ensureTestDependenciesExist() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Ensure airports
        ensureAirport(conn, "YYC", "Calgary International", "Calgary", "Canada", "MST");
        ensureAirport(conn, "YYZ", "Toronto Pearson", "Toronto", "Canada", "EST");

        // Ensure aircraft
        ensureAircraft(conn);

        // Ensure route
        ensureRoute(conn);

        // Ensure airline
        ensureAirline(conn);
    }

    private static void ensureAirport(Connection conn, String code, String name, String city, String country, String timezone) throws SQLException {
        String sql = "INSERT INTO airports (airport_code, name, city, country, timezone) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, city);
            ps.setString(4, country);
            ps.setString(5, timezone);
            ps.executeUpdate();
        }
    }

    private static void ensureAircraft(Connection conn) throws SQLException {
        String checkSql = "SELECT aircraft_id FROM aircraft WHERE model = 'TEST-737' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return; // Already exists
            }
        }

        String sql = "INSERT INTO aircraft (model, manufacturer, total_seats, seat_configuration, status) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "TEST-737");
            ps.setString(2, "Boeing");
            ps.setInt(3, 180);
            ps.setString(4, "3-3");
            ps.setString(5, "ACTIVE");
            ps.executeUpdate();
        }
    }

    private static void ensureRoute(Connection conn) throws SQLException {
        String checkSql = "SELECT route_id FROM routes WHERE origin_code = 'YYC' AND destination_code = 'YYZ' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return; // Already exists
            }
        }

        String sql = "INSERT INTO routes (origin_code, destination_code, distance_km, estimated_duration_minutes) " +
                    "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "YYC");
            ps.setString(2, "YYZ");
            ps.setDouble(3, 2700.0);
            ps.setInt(4, 240);
            ps.executeUpdate();
        }
    }

    private static void ensureAirline(Connection conn) throws SQLException {
        String checkSql = "SELECT airline_id FROM airlines WHERE code = 'AC' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return; // Already exists
            }
        }

        String sql = "INSERT INTO airlines (name, code, country) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "Air Canada");
            ps.setString(2, "AC");
            ps.setString(3, "Canada");
            ps.executeUpdate();
        }
    }

    private static int getTestAircraftId() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT aircraft_id FROM aircraft WHERE model = 'TEST-737' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("aircraft_id");
            }
        }
        throw new SQLException("Test aircraft not found");
    }

    private static int getTestRouteId() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT route_id FROM routes WHERE origin_code = 'YYC' AND destination_code = 'YYZ' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("route_id");
            }
        }
        throw new SQLException("Test route not found");
    }

    private static Integer getFlightIdByNumber(String flightNumber) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT flight_id FROM flights WHERE flight_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flightNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("flight_id");
                }
            }
        }
        return null;
    }
}

