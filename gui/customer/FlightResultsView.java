package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Flight;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Displays flight search results in a table.
 * Each row has a "Book" button that navigates to seat selection.
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
        
        JLabel title = new JLabel("Flight Search Results");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);
        
        String resultsMessage = flights.isEmpty() 
            ? "No flights found matching your criteria."
            : "Found " + flights.size() + " flight(s) matching your search.";
        
        JLabel resultsLabel = new JLabel(resultsMessage);
        resultsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        resultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resultsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(resultsLabel, BorderLayout.CENTER);
        
        if (flights.isEmpty()) {
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
        
        String[] columnNames = {
            "Flight Number", "Origin", "Destination", 
            "Departure", "Arrival", "Price", "Available Seats", "Action"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        
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
            
            tableModel.addRow(new Object[]{
                flight.getFlightNumber(),
                origin,
                destination,
                departure,
                arrival,
                String.format("$%.2f", flight.getPrice()),
                String.valueOf(flight.getAvailableSeats()),
                "Book"
            });
        }
        
        flightTable = new JTable(tableModel);
        flightTable.setRowHeight(30);
        flightTable.setFont(new Font("Arial", Font.PLAIN, 12));
        flightTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        flightTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        flightTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(flightTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        add(scrollPane, BorderLayout.CENTER);
        
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
            if (isPushed && selectedRow >= 0 && selectedRow < flights.size()) {
                handleBookFlight(flights.get(selectedRow));
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
    
    private void handleBookFlight(Flight flight) {
        viewManager.showView("SEAT_SELECTION", 
            new SeatSelectionView(viewManager, flight, flights));
    }
}
