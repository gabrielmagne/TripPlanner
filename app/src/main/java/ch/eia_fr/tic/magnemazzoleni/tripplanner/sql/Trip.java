package ch.eia_fr.tic.magnemazzoleni.tripplanner.sql;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Created by Dosky on 29.05.2015.
 */
public class Trip implements Serializable {
    private long id;

    volatile private LatLng departure;
    volatile private LatLng arrival;
    private double depLat;
    private double depLng;
    private double arrLat;
    private double arrLng;

    private int distance;
    private int duration;

    private String name;
    private int color;

    private String departureAddress;
    private String arrivalAddress;

    public Trip(long id, LatLng departure, LatLng arrival, int distance, int duration, String name, int color, String departureAddress, String arrivalAddress) {
        this.id = id;
        this.departure = departure;
        this.arrival = arrival;
        this.distance = distance;
        this.duration = duration;
        this.name = name;
        this.color = color;
        this.departureAddress = departureAddress;
        this.arrivalAddress = arrivalAddress;
    }

    public long getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public int getDistance() {
        return distance;
    }

    public int getDuration() {
        return duration;
    }

    public LatLng getArrival() {
        return arrival;
    }

    public LatLng getDeparture() {
        return departure;
    }

    public String getName() {
        return name;
    }

    public String getDepartureAddress() {
        return departureAddress;
    }

    public String getArrivalAddress() {
        return arrivalAddress;
    }

    @Override
    public String toString() {
        return getDepartureAddress() + " -> " + getArrivalAddress();
    }

    /*
     * Serialization helper
     */

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        depLat = departure.latitude;
        depLng = departure.longitude;
        arrLat = arrival.latitude;
        arrLng = arrival.longitude;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        departure = new LatLng(depLat, depLng);
        arrival = new LatLng(arrLat, arrLng);
    }

    private void readObjectNoData() throws ObjectStreamException {
        departure = new LatLng(0,0);
        arrival = new LatLng(0,0);
    }
}
