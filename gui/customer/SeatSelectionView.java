package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Flight;
import javax.swing.*;

/**
 * Seat selection interface.
 * Shows seat map and allows customers to select available seats.
 */
public class SeatSelectionView extends JPanel {
    
    private ViewManager viewManager;
    private Flight flight;
    
    public SeatSelectionView(ViewManager viewManager, Flight flight) {
        this.viewManager = viewManager;
        this.flight = flight;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement seat selection grid
    }
}

