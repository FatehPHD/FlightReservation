package businesslogic.entities;
public class Route {

    private int routeId;
    private Airport origin;
    private Airport destination;
    private double distance;          // e.g. in kilometers
    private int estimatedDuration;    // e.g. in minutes

    public Route() {
    }

    public Route(int routeId, Airport origin, Airport destination,
                 double distance, int estimatedDuration) {
        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.estimatedDuration = estimatedDuration;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public Airport getOrigin() {
        return origin;
    }

    public void setOrigin(Airport origin) {
        this.origin = origin;
    }

    public Airport getDestination() {
        return destination;
    }

    public void setDestination(Airport destination) {
        this.destination = destination;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @Override
    public String toString() {
        return "Route{" +
                "routeId=" + routeId +
                ", origin=" + (origin != null ? origin.getAirportCode() : "null") +
                ", destination=" + (destination != null ? destination.getAirportCode() : "null") +
                ", distance=" + distance +
                ", estimatedDuration=" + estimatedDuration +
                '}';
    }
}
