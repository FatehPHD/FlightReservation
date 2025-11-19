package tests;

import businesslogic.entities.Airport;
import businesslogic.entities.Route;
import datalayer.dao.RouteDAO;
import datalayer.impl.RouteDAOImpl;
import datalayer.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TestRouteDAO {

    public static void main(String[] args) {
        try {
            // 1) Ensure required airports exist in DB
            ensureTestAirportsExist();

            // 2) Create Airport objects for the Route entity
            Airport yyc = new Airport();
            yyc.setAirportCode("YYC");

            Airport yvr = new Airport();
            yvr.setAirportCode("YVR");

            // 3) Create a Route object
            Route route = new Route();
            route.setOrigin(yyc);
            route.setDestination(yvr);
            route.setDistance(700.0);
            route.setEstimatedDuration(90);

            RouteDAO routeDAO = new RouteDAOImpl();

            System.out.println("Saving route...");
            routeDAO.save(route);
            System.out.println("Saved: " + route);

            // 4) Find by ID
            System.out.println("Finding by ID...");
            Route found = routeDAO.findById(route.getRouteId());
            System.out.println("Found: " + found);

            // 5) Find all
            System.out.println("Finding all routes...");
            List<Route> routes = routeDAO.findAll();
            for (Route r : routes) {
                System.out.println(r);
            }

            // 6) Update
            System.out.println("Updating route distance and duration...");
            route.setDistance(750.0);
            route.setEstimatedDuration(95);
            boolean updated = routeDAO.update(route);
            System.out.println("Updated? " + updated);

            Route updatedRoute = routeDAO.findById(route.getRouteId());
            System.out.println("After update: " + updatedRoute);

            // 7) Delete
            System.out.println("Deleting route...");
            boolean deleted = routeDAO.delete(route.getRouteId());
            System.out.println("Deleted? " + deleted);

            Route afterDelete = routeDAO.findById(route.getRouteId());
            System.out.println("After delete (should be null): " + afterDelete);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ensureTestAirportsExist() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        String upsertAirportSql =
                "INSERT INTO airports (airport_code, name, city, country, timezone) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), city = VALUES(city), country = VALUES(country), timezone = VALUES(timezone)";

        try (PreparedStatement ps = conn.prepareStatement(upsertAirportSql)) {
            // YYC
            ps.setString(1, "YYC");
            ps.setString(2, "Calgary International Airport");
            ps.setString(3, "Calgary");
            ps.setString(4, "Canada");
            ps.setString(5, "MST");
            ps.executeUpdate();

            // YVR
            ps.setString(1, "YVR");
            ps.setString(2, "Vancouver International Airport");
            ps.setString(3, "Vancouver");
            ps.setString(4, "Canada");
            ps.setString(5, "PST");
            ps.executeUpdate();
        }
    }
}
