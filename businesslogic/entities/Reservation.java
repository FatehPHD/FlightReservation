package businesslogic.entities;

import java.time.LocalDateTime;
import java.util.List;

import businesslogic.entities.enums.ReservationStatus;

public class Reservation {

    private int reservationId;
    private LocalDateTime bookingDate;
    private ReservationStatus status;
    private double totalPrice;

    private Customer customer;
    private Flight flight;
    private Payment payment;
    private List<Seat> seats;

    public Reservation() {
    }

    public Reservation(int reservationId,
                       LocalDateTime bookingDate,
                       ReservationStatus status,
                       double totalPrice,
                       Customer customer,
                       Flight flight,
                       Payment payment,
                       List<Seat> seats) {

        this.reservationId = reservationId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalPrice = totalPrice;
        this.customer = customer;
        this.flight = flight;
        this.payment = payment;
        this.seats = seats;
    }

    // ----------- GETTERS -----------

    public int getReservationId() {
        return reservationId;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Flight getFlight() {
        return flight;
    }

    public Payment getPayment() {
        return payment;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    // ----------- SETTERS (MISSING ONES ADDED) -----------

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
