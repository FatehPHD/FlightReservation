package businesslogic.entities;

import businesslogic.entities.enums.SeatClass;

public class Seat {

    private int seatId;
    private String seatNumber;    // e.g. "12A"
    private SeatClass seatClass;  // ECONOMY / BUSINESS / FIRST
    private boolean isAvailable;
    private int flightId; 

    public Seat() {
    }

    public Seat(int seatId, String seatNumber, SeatClass seatClass, boolean isAvailable) {
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.isAvailable = isAvailable;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public long getFlightId() {
    return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "seatId=" + seatId +
                ", seatNumber='" + seatNumber + '\'' +
                ", seatClass=" + seatClass +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
