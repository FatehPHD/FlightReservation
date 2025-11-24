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

    public Reservation(int reservationId, LocalDateTime bookingDate, ReservationStatus status,
                       double totalPrice, Customer customer, Flight flight,
                       Payment payment, List<Seat> seats) {
        this.reservationId = reservationId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalPrice = totalPrice;
        this.customer = customer;
        this.flight = flight;
        this.payment = payment;
        this.seats = seats;
    }

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

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}
