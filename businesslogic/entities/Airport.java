package businesslogic.entities;
public class Airport {

    private String airportCode;
    private String name;
    private String city;
    private String country;
    private String timezone;

    public Airport() {
    }

    public Airport(String airportCode, String name, String city, String country, String timezone) {
        this.airportCode = airportCode;
        this.name = name;
        this.city = city;
        this.country = country;
        this.timezone = timezone;
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return "Airport{" +
                "airportCode='" + airportCode + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", timezone='" + timezone + '\'' +
                '}';
    }
}
