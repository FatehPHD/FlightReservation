package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Flight;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Display search results in a table.
 * Shows available flights with book button for each.
 */
public class FlightResultsView extends JPanel {
    
    private ViewManager viewManager;
    private List<Flight> flights;
    private JTable flightTable;
    private DefaultTableModel tableModel;
    
    public FlightResultsView(ViewManager viewManager, List<Flight> flights) {
        this.viewManager = viewManager;
        this.flights = flights;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title
        JLabel title = new JLabel("Flight Search Results");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);
        
        // Results message
        String resultsMessage;
        if (flights.isEmpty()) {
            resultsMessage = "No flights found matching your criteria.";
        } else {
            resultsMessage = "Found " + flights.size() + " flight(s) matching your search.";
        }
        JLabel resultsLabel = new JLabel(resultsMessage);
        resultsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        resultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(resultsLabel, BorderLayout.CENTER);
        
        if (flights.isEmpty()) {
            // Show back button if no results
            JPanel buttonPanel = new JPanel();
            JButton backBtn = new JButton("Back to Search");
            backBtn.setPreferredSize(new Dimension(200, 35));
            backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
            backBtn.addActionListener(e -> {
                viewManager.showView("FLIGHT_SEARCH", 
                    new FlightSearchView(viewManager));
            });
            buttonPanel.add(backBtn);
            add(buttonPanel, BorderLayout.SOUTH);
            return;
        }
        
        // Create table model
        String[] columnNames = {
            "Flight Number", "Origin", "Destination", 
            "Departure", "Arrival", "Price", "Available Seats", "Action"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only action column is "editable" (for button)
            }
        };
        
        // Populate table
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Flight flight : flights) {
            String origin = flight.getRoute() != null && flight.getRoute().getOrigin() != null
                ? flight.getRoute().getOrigin().getAirportCode() : "N/A";
            String destination = flight.getRoute() != null && flight.getRoute().getDestination() != null
                ? flight.getRoute().getDestination().getAirportCode() : "N/A";
            String departure = flight.getDepartureTime() != null
                ? flight.getDepartureTime().format(formatter) : "N/A";
            String arrival = flight.getArrivalTime() != null
                ? flight.getArrivalTime().format(formatter) : "N/A";
            String price = String.format("$%.2f", flight.getPrice());
            String availableSeats = String.valueOf(flight.getAvailableSeats());
            
            tableModel.addRow(new Object[]{
                flight.getFlightNumber(),
                origin,
                destination,
                departure,
                arrival,
                price,
                availableSeats,
                "Book"
            });
        }
        
        // Create table
        flightTable = new JTable(tableModel);
        flightTable.setRowHeight(30);
        flightTable.setFont(new Font("Arial", Font.PLAIN, 12));
        flightTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add button renderer and editor for action column
        flightTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        flightTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton backBtn = new JButton("Back to Search");
        backBtn.setPreferredSize(new Dimension(200, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("FLIGHT_SEARCH", 
                new FlightSearchView(viewManager));
        });
        buttonPanel.add(backBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Button renderer for table cells.
     */
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    
    /**
     * Button editor for table cells.
     */
    private class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int selectedRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            selectedRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Handle book button click
                if (selectedRow >= 0 && selectedRow < flights.size()) {
                    Flight selectedFlight = flights.get(selectedRow);
                    handleBookFlight(selectedFlight);
                }
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
        
        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
    
    /**
     * Handle booking a flight - navigate to seat selection.
     */
    private void handleBookFlight(Flight flight) {
        viewManager.showView("SEAT_SELECTION", 
            new SeatSelectionView(viewManager, flight));
    }
}
