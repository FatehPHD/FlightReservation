// File: tests/TestSeatDAO.java
package tests;

import businesslogic.entities.Aircraft;
import businesslogic.entities.Airline;
import businesslogic.entities.Airport;
import businesslogic.entities.Flight;
import businesslogic.entities.Route;
import businesslogic.entities.Seat;
import businesslogic.entities.enums.FlightStatus;
import businesslogic.entities.enums.SeatClass;
import datalayer.dao.*;
import datalayer.impl.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TestSeatDAO {

    public static void main(String[] args) throws SQLException{
        // ----- DAOs -----
        AirlineDAO airlineDAO = new AirlineDAOImpl();
        AirportDAO airportDAO = new AirportDAOImpl();
        RouteDAO routeDAO = new RouteDAOImpl();
        AircraftDAO aircraftDAO = new AircraftDAOImpl();
        FlightDAO flightDAO = new FlightDAOImpl();
        SeatDAO seatDAO = new SeatDAOImpl();

        try {
            // Use a suffix to avoid unique-constraint issues on repeated runs
            String suffix = String.valueOf(System.currentTimeMillis() % 100000);

            System.out.println("==== SETUP: SUPPORTING DATA FOR FLIGHT ====");

            // ----- Airline -----
            Airline airline = new Airline();
            airline.setName("SeatTest Airline " + suffix);
            airline.setCode("ST" + suffix);    // must be unique in airlines.code
            airline.setCountry("Canada");
            airline = airlineDAO.save(airline);
            System.out.println("Created airline: " + airline);

            // ----- Airports -----
            Airport origin = new Airport();
            origin.setAirportCode("SO" + suffix);      // unique
            origin.setName("SeatTest Origin " + suffix);
            origin.setCity("Origin City");
            origin.setCountry("Canada");
            origin.setTimezone("MST");
            origin = airportDAO.save(origin);
            System.out.println("Created origin airport: " + origin);

            Airport destination = new Airport();
            destination.setAirportCode("SD" + suffix);
            destination.setName("SeatTest Dest " + suffix);
            destination.setCity("Destination City");
            destination.setCountry("Canada");
            destination.setTimezone("PST");
            destination = airportDAO.save(destination);
            System.out.println("Created destination airport: " + destination);

            // ----- Route -----
            Route route = new Route();
            route.setOrigin(origin);
            route.setDestination(destination);
            route.setDistance(750.0);
            route.setEstimatedDuration(100);   // in minutes
            route = routeDAO.save(route);
            System.out.println("Created route: " + route);

            // ----- Aircraft -----
            Aircraft aircraft = new Aircraft();
            aircraft.setModel("737-800");
            aircraft.setManufacturer("Boeing");
            aircraft.setTotalSeats(180);
            aircraft.setSeatConfiguration("3-3");
            aircraft.setStatus("ACTIVE");
            aircraft = aircraftDAO.save(aircraft);
            System.out.println("Created aircraft: " + aircraft);

            // ----- Flight (using your FlightDAO / Impl) -----
            Flight flight = new Flight();
            flight.setFlightNumber("SF" + suffix);
            flight.setDepartureTime(LocalDateTime.now().plusDays(1));
            flight.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(2));
            flight.setStatus(FlightStatus.SCHEDULED);
            flight.setAvailableSeats(aircraft.getTotalSeats());
            flight.setPrice(299.99);
            flight.setAircraft(aircraft);
            flight.setRoute(route);
            flight.setAirline(airline);

            flight = flightDAO.save(flight);
            System.out.println("Created flight: " + flight);

            int flightId = flight.getFlightId();   

            // =====================================================================
            //                           SEAT TESTS
            // =====================================================================

            System.out.println("\n==== TEST: SAVE SEAT ====");
            Seat seat = new Seat();
            seat.setSeatNumber("12A");
            seat.setSeatClass(SeatClass.ECONOMY);
            seat.setAvailable(true);
            seat.setFlightId(flightId);   // link to the flight we just created

            Seat savedSeat = seatDAO.save(seat);
            System.out.println("Saved seat: " + savedSeat);

            System.out.println("\n==== TEST: FIND BY ID ====");
            Seat foundById = seatDAO.findById(savedSeat.getSeatId());
            System.out.println("Found by ID: " + foundById);

            System.out.println("\n==== TEST: FIND BY FLIGHT ID ====");
            List<Seat> seatsByFlight = seatDAO.findByFlightId(flightId);
            for (Seat s : seatsByFlight) {
                System.out.println(s);
            }

            System.out.println("\n==== TEST: FIND AVAILABLE BY FLIGHT ====");
            List<Seat> availableSeats = seatDAO.findAvailableByFlight(flightId);
            for (Seat s : availableSeats) {
                System.out.println("Available: " + s);
            }

            System.out.println("\n==== TEST: FIND BY FLIGHT + SEAT NUMBER ====");
            Seat foundByFlightAndNumber =
                    seatDAO.findByFlightAndSeatNumber(flightId, "12A");
            System.out.println("Found seat: " + foundByFlightAndNumber);

            System.out.println("\n==== TEST: UPDATE AVAILABILITY ====");
            boolean availabilityUpdated =
                    seatDAO.updateAvailability(savedSeat.getSeatId(), false);
            System.out.println("Availability updated? " + availabilityUpdated);
            Seat afterAvailUpdate = seatDAO.findById(savedSeat.getSeatId());
            System.out.println("After availability update: " + afterAvailUpdate);

            System.out.println("\n==== TEST: FULL UPDATE (CHANGE CLASS) ====");
            afterAvailUpdate.setSeatClass(SeatClass.BUSINESS);
            boolean updated = seatDAO.update(afterAvailUpdate);
            System.out.println("Full update success? " + updated);
            Seat afterFullUpdate = seatDAO.findById(savedSeat.getSeatId());
            System.out.println("After full update: " + afterFullUpdate);

            System.out.println("\n==== TEST: DELETE SEAT ====");
            boolean deleted = seatDAO.delete(savedSeat.getSeatId());
            System.out.println("Deleted? " + deleted);
            Seat afterDelete = seatDAO.findById(savedSeat.getSeatId());
            System.out.println("Should be null after delete: " + afterDelete);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
