package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Flight;
import javax.swing.*;
import java.util.List;

/**
 * Display search results in a table.
 * Shows available flights with book button for each.
 */
public class FlightResultsView extends JPanel {
    
    private ViewManager viewManager;
    private List<Flight> flights;
    
    public FlightResultsView(ViewManager viewManager, List<Flight> flights) {
        this.viewManager = viewManager;
        this.flights = flights;
        initComponents();
    }
    
    private void initComponents() {
        // TODO: Implement flight results table
    }
}

