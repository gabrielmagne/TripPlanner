package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dosky on 29.05.2015.
 */
public class Trip {
    long id;

    LatLng departure;
    LatLng arrive;

    double distance;

    String name;
    Color color;

    String departureAddress;
    String arriveAddress;

    public long getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public double getDistance() {
        return distance;
    }

    public LatLng getArrive() {
        return arrive;
    }

    public LatLng getDeparture() {
        return departure;
    }

    public String getName() {
        return name;
    }

    public void setArrive(LatLng arrive) {
        this.arrive = arrive;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setDeparture(LatLng departure) {
        this.departure = departure;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setName(String name) {
        this.name = name;
    }
}
