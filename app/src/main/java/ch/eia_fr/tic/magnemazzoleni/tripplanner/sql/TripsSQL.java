package ch.eia_fr.tic.magnemazzoleni.tripplanner.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dosky on 03.06.2015.
 */
public class TripsSQL {
    private SQLiteDatabase db;
    private TripSQLHelper taskHelper;

    public TripsSQL(Context context) {
        taskHelper = new TripSQLHelper(context);
    }

    private void open() {
        db = taskHelper.getWritableDatabase();
    }

    private  void close() {
        db.close();
    }

    public Trip insert(LatLng departure, LatLng arrival, int distance, int duration, String name, int color, String departureAddress, String arrivalAddress) {
        ContentValues values = new ContentValues();
        values.put(TripSQLHelper.COL_NAME, name);
        values.put(TripSQLHelper.COL_DEP_ADDRESS, departureAddress);
        values.put(TripSQLHelper.COL_ARR_ADDRESS, arrivalAddress);
        values.put(TripSQLHelper.COL_DEP_LAT, departure.latitude);
        values.put(TripSQLHelper.COL_DEP_LNG, departure.longitude);
        values.put(TripSQLHelper.COL_ARR_LAT, arrival.latitude);
        values.put(TripSQLHelper.COL_ARR_LNG, arrival.longitude);
        values.put(TripSQLHelper.COL_DISTANCE, distance);
        values.put(TripSQLHelper.COL_DURATION, duration);
        values.put(TripSQLHelper.COL_COLOR, color);

        open();
        long id = db.insert(TripSQLHelper.TABLE_NAME, null, values);

        Trip item = get(id);
        close();

        return item;
    }

    public void delete(Trip item) {
        open();
        db.delete(TripSQLHelper.TABLE_NAME, TripSQLHelper.COL_ID + "=" + item.getId(), null);
        close();
    }

    public Trip get(long id) {
        open();
        Cursor cursor = db.query(TripSQLHelper.TABLE_NAME, TripSQLHelper.COLS, TripSQLHelper.COL_ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();
        long did = cursor.getLong(0);
        String name = cursor.getString(1);
        String departureAddress = cursor.getString(2);
        String arrivalAddress = cursor.getString(3);
        LatLng departure = new LatLng(cursor.getDouble(4), cursor.getDouble(5));
        LatLng arrival = new LatLng(cursor.getDouble(6), cursor.getDouble(7));
        int distance = cursor.getInt(8);
        int duration = cursor.getInt(9);
        int color = cursor.getInt(10);

        Trip p = new Trip(did, departure, arrival, distance, duration, name, color, departureAddress, arrivalAddress);
        cursor.close();
        close();
        return  p;
    }

    public Trip update(Trip item) {
        ContentValues upd = new ContentValues();
        upd.put(TripSQLHelper.COL_NAME, item.getName());
        upd.put(TripSQLHelper.COL_DEP_ADDRESS, item.getDepartureAddress());
        upd.put(TripSQLHelper.COL_ARR_ADDRESS, item.getArrivalAddress());
        upd.put(TripSQLHelper.COL_DEP_LAT, item.getDeparture().latitude);
        upd.put(TripSQLHelper.COL_DEP_LNG, item.getDeparture().longitude);
        upd.put(TripSQLHelper.COL_ARR_LAT, item.getArrival().latitude);
        upd.put(TripSQLHelper.COL_ARR_LNG, item.getArrival().longitude);
        upd.put(TripSQLHelper.COL_DISTANCE, item.getDistance());
        upd.put(TripSQLHelper.COL_DURATION, item.getDuration());
        upd.put(TripSQLHelper.COL_COLOR, item.getColor());

        open();
        db.update(TripSQLHelper.TABLE_NAME, upd, TripSQLHelper.COL_ID + "=" + item.getId(), null);
        close();

        return get(item.getId());
    }

    public List<Trip> getAll() {
        open();
        Cursor cursor = db.query(TripSQLHelper.TABLE_NAME, TripSQLHelper.COLS, null, null, null, null, TripSQLHelper.COL_ID + " DESC");
        cursor.moveToFirst();
        List<Trip> tasks = new ArrayList<Trip>();
        while(!cursor.isAfterLast()) {
            long id = cursor.getLong(0);
            String name = cursor.getString(1);
            String departureAddress = cursor.getString(2);
            String arrivalAddress = cursor.getString(3);
            LatLng departure = new LatLng(cursor.getDouble(4), cursor.getDouble(5));
            LatLng arrival = new LatLng(cursor.getDouble(6), cursor.getDouble(7));
            int distance = cursor.getInt(8);
            int duration = cursor.getInt(9);
            int color = cursor.getInt(10);

            Trip p = new Trip(id, departure, arrival, distance, duration, name, color, departureAddress, arrivalAddress);
            tasks.add(p);
            cursor.moveToNext();
        }
        cursor.close();
        close();
        return tasks;
    }
}