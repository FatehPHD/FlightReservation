// File: tests/TestFlightDAO.java
package tests;

import businesslogic.entities.Aircraft;
import businesslogic.entities.Airline;
import businesslogic.entities.Airport;
import businesslogic.entities.Flight;
import businesslogic.entities.Route;
import businesslogic.entities.enums.FlightStatus;

import datalayer.dao.AircraftDAO;
import datalayer.dao.AirlineDAO;
import datalayer.dao.AirportDAO;
import datalayer.dao.RouteDAO;
import datalayer.dao.FlightDAO;

import datalayer.impl.AircraftDAOImpl;
import datalayer.impl.AirlineDAOImpl;
import datalayer.impl.AirportDAOImpl;
import datalayer.impl.RouteDAOImpl;
import datalayer.impl.FlightDAOImpl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TestFlightDAO {

    public static void main(String[] args) throws SQLException{
        AirlineDAO airlineDAO = new AirlineDAOImpl();
        AirportDAO airportDAO = new AirportDAOImpl();
        RouteDAO routeDAO = new RouteDAOImpl();
        AircraftDAO aircraftDAO = new AircraftDAOImpl();
        FlightDAO flightDAO = new FlightDAOImpl();

        try {
            System.out.println("==== SETUP: SUPPORTING DATA ====");

            // 1) Airline
            Airline airline = new Airline();
            airline.setName("Test Airline");
            airline.setCode("TA" + (System.currentTimeMillis() % 100000));
            airline.setCountry("Canada");

            airline = airlineDAO.save(airline);
            System.out.println("Created airline: " + airline);

            // 2) Airports with unique codes per run
            String originCode = "O" + (System.currentTimeMillis() % 100000);
            String destCode = "D" + (System.currentTimeMillis() % 100000);

            Airport origin = new Airport(
                    originCode,
                    "Origin Airport",
                    "Origin City",
                    "Canada",
                    "MST"
            );

            Airport destination = new Airport(
                    destCode,
                    "Destination Airport",
                    "Destination City",
                    "Canada",
                    "PST"
            );

            origin = airportDAO.save(origin);
            destination = airportDAO.save(destination);

            System.out.println("Created origin airport: " + origin);
            System.out.println("Created destination airport: " + destination);

            // 3) Route
            Route route = new Route();
            route.setOrigin(origin);
            route.setDestination(destination);
            // FIXED: use setDistance and setEstimatedDuration from your Route.java
            route.setDistance(750.0);          // double
            route.setEstimatedDuration(100);   // int (minutes)

            route = routeDAO.save(route);
            System.out.println("Created route: " + route);

            // 4) Aircraft
            Aircraft aircraft = new Aircraft();
            aircraft.setModel("737-800");
            aircraft.setManufacturer("Boeing");
            aircraft.setTotalSeats(180);
            aircraft.setSeatConfiguration("3-3");
            aircraft.setStatus("ACTIVE");

            aircraft = aircraftDAO.save(aircraft);
            System.out.println("Created aircraft: " + aircraft);

            System.out.println("\n==== TEST: SAVE FLIGHT ====");

            LocalDateTime now = LocalDateTime.now();
            String flightNumber = "FN" + (System.currentTimeMillis() % 100000);

            Flight flight = new Flight(
                    flightNumber,
                    now.plusDays(1),
                    now.plusDays(1).plusHours(2),
                    FlightStatus.SCHEDULED,
                    aircraft.getTotalSeats(),
                    299.99,
                    aircraft,
                    route
            );
            // Set airline via setter
            flight.setAirline(airline);

            Flight savedFlight = flightDAO.save(flight);
            System.out.println("Saved flight: " + savedFlight);

            System.out.println("\n==== TEST: FIND BY ID (flight_id) ====");
            Flight byId = flightDAO.findById(savedFlight.getFlightId());
            System.out.println("Found by ID: " + byId);

            System.out.println("\n==== TEST: FIND BY FLIGHT NUMBER ====");
            Flight byNumber = flightDAO.findByFlightNumber(flightNumber);
            System.out.println("Found by number: " + byNumber);

            System.out.println("\n==== TEST: UPDATE FLIGHT ====");
            byId.setStatus(FlightStatus.DELAYED);
            byId.setPrice(249.99);
            boolean updated = flightDAO.update(byId);
            System.out.println("Updated? " + updated);

            Flight afterUpdate = flightDAO.findById(byId.getFlightId());
            System.out.println("After update: " + afterUpdate);

            System.out.println("\n==== TEST: FIND BY STATUS (DELAYED) ====");
            List<Flight> delayedFlights = flightDAO.findByStatus(FlightStatus.DELAYED);
            for (Flight f : delayedFlights) {
                System.out.println(f);
            }

            System.out.println("\n==== TEST: FIND BY ROUTE (" + originCode + " -> " + destCode + ") ====");
            List<Flight> routeFlights = flightDAO.findByRoute(originCode, destCode);
            for (Flight f : routeFlights) {
                System.out.println(f);
            }

            System.out.println("\n==== TEST: FIND BY DEPARTURE RANGE ====");
            List<Flight> rangeFlights = flightDAO.findByDepartureRange(
                    now,
                    now.plusDays(2)
            );
            for (Flight f : rangeFlights) {
                System.out.println(f);
            }

            System.out.println("\n==== TEST: DELETE FLIGHT ====");
            boolean deleted = flightDAO.delete(savedFlight.getFlightId());
            System.out.println("Deleted? " + deleted);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
