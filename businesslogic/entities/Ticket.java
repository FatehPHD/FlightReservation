package businesslogic.entities;

import java.time.LocalDateTime;

public class Ticket {

    private String ticketNumber;
    private LocalDateTime issueDate;
    private String passengerName;

    private Reservation reservation;
    private Seat seat;
    private String barcode;

    public Ticket(String ticketNumber, LocalDateTime issueDate, String passengerName,
                  Reservation reservation, Seat seat, String barcode) {
        this.ticketNumber = ticketNumber;
        this.issueDate = issueDate;
        this.passengerName = passengerName;
        this.reservation = reservation;
        this.seat = seat;
        this.barcode = barcode;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Seat getSeat() {
        return seat;
    }

    public String getBarcode() {
        return barcode;
    }
}
