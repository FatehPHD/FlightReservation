package businesslogic.entities;

import java.time.LocalDateTime;

import businesslogic.entities.enums.FlightStatus;

public class Flight {

    private String flightNumber;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private FlightStatus status;
    private int availableSeats;
    private double price;
    private Aircraft aircraft;
    private Route route;

    public Flight() {
    }

    public Flight(String flightNumber,
                  LocalDateTime departureTime,
                  LocalDateTime arrivalTime,
                  FlightStatus status,
                  int availableSeats,
                  double price,
                  Aircraft aircraft,
                  Route route) {

        this.flightNumber = flightNumber;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.status = status;
        this.availableSeats = availableSeats;
        this.price = price;
        this.aircraft = aircraft;
        this.route = route;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public void setAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    @Override
    public String toString() {
        return "Flight{" +
               "flightNumber='" + flightNumber + '\'' +
               ", departureTime=" + departureTime +
               ", arrivalTime=" + arrivalTime +
               ", status=" + status +
               ", availableSeats=" + availableSeats +
               ", price=" + price +
               ", aircraft=" + (aircraft != null ? aircraft.getModel() : "null") +
               ", route=" + (route != null ? route.getOrigin() + "->" + route.getDestination() : "null") +
               '}';
    }
}
