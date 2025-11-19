package tests;

import businesslogic.entities.Airport;
import datalayer.impl.AirportDAOImpl;
import datalayer.dao.AirportDAO;

import java.sql.SQLException;
import java.util.List;

public class TestAirportDAO {

    public static void main(String[] args) throws Exception {
        AirportDAO airportDAO = new AirportDAOImpl();

        String testCode = "YYC";

        // Clean up any previous run so you don't get duplicate key errors
        try {
            airportDAO.delete(testCode);
        } catch (SQLException e) {
            // ignore if not present
        }

        // 1. Create and save an airport
        Airport yyc = new Airport(
                testCode,
                "Calgary International Airport",
                "Calgary",
                "Canada",
                "America/Edmonton"
        );

        System.out.println("Saving airport...");
        airportDAO.save(yyc);
        System.out.println("Saved.");

        // 2. Find by code
        System.out.println("Finding airport by code YYC...");
        Airport found = airportDAO.findByCode(testCode);
        if (found != null) {
            System.out.println("Found: " + found.getAirportCode() + " - " + found.getName());
        } else {
            System.out.println("Airport not found!");
        }

        // 3. Update airport
        System.out.println("Updating airport name...");
        found.setName("Calgary Intl Airport (Updated)");
        airportDAO.update(found);

        Airport updated = airportDAO.findByCode(testCode);
        System.out.println("Updated: " + updated.getAirportCode() + " - " + updated.getName());

        // 4. List all airports
        System.out.println("Listing all airports...");
        List<Airport> airports = airportDAO.findAll();
        for (Airport a : airports) {
            System.out.println(a.getAirportCode() + " | " + a.getName() + " | " + a.getCity() + " | " + a.getCountry());
        }

        // 5. Delete airport
        System.out.println("Deleting airport YYC...");
        airportDAO.delete(testCode);

        Airport deleted = airportDAO.findByCode(testCode);
        System.out.println("After delete, found = " + deleted);
    }
}
