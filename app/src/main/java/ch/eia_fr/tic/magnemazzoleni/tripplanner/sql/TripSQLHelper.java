package ch.eia_fr.tic.magnemazzoleni.tripplanner.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Dosky on 03.06.2015.
 */
public class TripSQLHelper extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "trips";

    public static final String COL_ID           = "id";
    public static final String COL_NAME         = "name";
    public static final String COL_DEP_ADDRESS  = "departure";
    public static final String COL_ARR_ADDRESS  = "arrival";
    public static final String COL_DEP_LAT      = "dep_lat";
    public static final String COL_DEP_LNG      = "dep_lng";
    public static final String COL_ARR_LAT      = "dep_lat";
    public static final String COL_ARR_LNG      = "dep_lng";
    public static final String COL_DISTANCE     = "distance";
    public static final String COL_COLOR        = "color";

    public static final String DB_NAME = TABLE_NAME + ".sqlite";
    public static final int   DB_VERSION = 2;
    public static final String DB_CREATE = String.format(
        "create table %s ( " +
            "%s integer primary key autoincrement, " +
            "%s text not null," +
            "%s text not null," +
            "%s text not null," +
            "%s real not null," +
            "%s real not null," +
            "%s real not null," +
            "%s real not null," +
            "%s real not null," +
            "%s integer not null" +
        " );",
            TABLE_NAME,
            COL_ID,
            COL_NAME,
            COL_DEP_ADDRESS,
            COL_ARR_ADDRESS,
            COL_DEP_LAT,
            COL_DEP_LNG,
            COL_ARR_LAT,
            COL_ARR_LNG,
            COL_DISTANCE,
            COL_COLOR
    );

    public static final String[] COLS = new String[] {
            COL_ID,
            COL_NAME,
            COL_DEP_ADDRESS,
            COL_ARR_ADDRESS,
            COL_DEP_LAT,
            COL_DEP_LNG,
            COL_ARR_LAT,
            COL_ARR_LNG,
            COL_DISTANCE,
            COL_COLOR
    };

    public TripSQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        // create again
        onCreate(db);
    }
}
