package ch.eia_fr.tic.magnemazzoleni.tripplanner.sql;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dosky on 29.05.2015.
 */
public class Trip {
    long id;

    LatLng departure;
    LatLng arrival;

    double distance;

    String name;
    int color;

    String departureAddress;
    String arrivalAddress;

    public Trip(long id, LatLng departure, LatLng arrival, double distance, String name, int color, String departureAddress, String arrivalAddress) {
        this.id = id;
        this.departure = departure;
        this.arrival = arrival;
        this.distance = distance;
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

    public double getDistance() {
        return distance;
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
        return getName();
    }
}
