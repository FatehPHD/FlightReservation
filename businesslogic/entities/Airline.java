package businesslogic.entities;
public class Airline {

    private int airlineId;
    private String name;
    private String code;     // e.g. "AC" for Air Canada
    private String country;

    public Airline() {
    }

    public Airline(int airlineId, String name, String code, String country) {
        this.airlineId = airlineId;
        this.name = name;
        this.code = code;
        this.country = country;
    }

    public int getAirlineId() {
        return airlineId;
    }

    public void setAirlineId(int airlineId) {
        this.airlineId = airlineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Airline{" +
                "airlineId=" + airlineId +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
