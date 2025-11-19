package businesslogic.entities;

import businesslogic.entities.enums.SeatClass;

public class EntityTest {

    public static void main(String[] args) {
        // ----- Test Airport -----
        Airport yyc = new Airport("YYC", "Calgary International Airport",
                "Calgary", "Canada", "America/Edmonton");

        Airport yvr = new Airport("YVR", "Vancouver International Airport",
                "Vancouver", "Canada", "America/Vancouver");

        System.out.println("=== Airports ===");
        System.out.println(yyc);
        System.out.println(yvr);

        // ----- Test Airline -----
        Airline airCanada = new Airline(1, "Air Canada", "AC", "Canada");

        System.out.println("\n=== Airline ===");
        System.out.println(airCanada);

        // ----- Test Route -----
        Route yycToYvr = new Route(
                1001,
                yyc,
                yvr,
                686.0,      // approx distance in km
                90          // approx duration in minutes
        );

        System.out.println("\n=== Route ===");
        System.out.println(yycToYvr);

        // ----- Test Aircraft -----
        Aircraft boeing737 = new Aircraft(
                10,
                "737-800",
                "Boeing",
                160,
                "3-3",
                "ACTIVE"
        );

        System.out.println("\n=== Aircraft ===");
        System.out.println(boeing737);

        // ----- Test Seat -----
        Seat seat12A = new Seat(
                10001,
                "12A",
                SeatClass.ECONOMY,
                true
        );

        Seat seat1A = new Seat(
                10002,
                "1A",
                SeatClass.FIRST,
                false
        );

        System.out.println("\n=== Seats ===");
        System.out.println(seat12A);
        System.out.println(seat1A);

        // ----- Small sanity check summary -----
        System.out.println("\n=== Summary Checks ===");
        System.out.println("Route origin: " + yycToYvr.getOrigin().getAirportCode());
        System.out.println("Route destination: " + yycToYvr.getDestination().getAirportCode());
        System.out.println("Aircraft model: " + boeing737.getModel());
        System.out.println("Seat 12A available? " + seat12A.isAvailable());
    }
}
