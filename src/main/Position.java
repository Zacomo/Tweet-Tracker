package main;

/*
Classe molto semplice che non sarebbe stata necessaria se non fosse per il fatto che il costruttore di LatLong
dà nullpointer exception se richiamato nel main controller. Non ho trovato altre soluzioni.
 */

public class Position {
    private double latitude;
    private double longitude;
    private String description;

    Position(double latitude, double longitude, String description){
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
