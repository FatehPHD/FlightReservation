package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Flight;
import businesslogic.entities.Seat;
import javax.swing.*;
import java.util.List;

/**
 * Payment processing form.
 * Collects payment details and processes booking payment.
 * Receives Flight and selected Seats, will create Reservation after payment.
 */
public class PaymentView extends JPanel {
    
    private ViewManager viewManager;
    private Flight flight;
    private List<Seat> selectedSeats;
    
    public PaymentView(ViewManager viewManager, Flight flight, List<Seat> selectedSeats) {
        this.viewManager = viewManager;
        this.flight = flight;
        this.selectedSeats = selectedSeats;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement payment form
        // Will create reservation using ReservationService.createReservationForUser()
        // after payment is processed
    }
}

