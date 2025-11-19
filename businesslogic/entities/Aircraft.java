package businesslogic.entities;
public class Aircraft {

    private int aircraftId;
    private String model;             // e.g. "737-800"
    private String manufacturer;      // e.g. "Boeing"
    private int totalSeats;
    private String seatConfiguration; // e.g. "3-3", "2-4-2"
    private String status;            // e.g. "ACTIVE", "IN_MAINTENANCE"

    public Aircraft() {
    }

    public Aircraft(int aircraftId, String model, String manufacturer,
                    int totalSeats, String seatConfiguration, String status) {
        this.aircraftId = aircraftId;
        this.model = model;
        this.manufacturer = manufacturer;
        this.totalSeats = totalSeats;
        this.seatConfiguration = seatConfiguration;
        this.status = status;
    }

    public int getAircraftId() {
        return aircraftId;
    }

    public void setAircraftId(int aircraftId) {
        this.aircraftId = aircraftId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String getSeatConfiguration() {
        return seatConfiguration;
    }

    public void setSeatConfiguration(String seatConfiguration) {
        this.seatConfiguration = seatConfiguration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Aircraft{" +
                "aircraftId=" + aircraftId +
                ", model='" + model + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", totalSeats=" + totalSeats +
                ", seatConfiguration='" + seatConfiguration + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
