package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.MenuItem;

import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.Trip;

public class TripActivity extends AppCompatActivity
        implements TripFragment.OnFragmentInteractionListener,
                     TripAdd.OnFragmentInteractionListener,
                     TripInfo.OnFragmentInteractionListener,
                     TripMap.OnFragmentInteractionListener,
                     FragmentManager.OnBackStackChangedListener {

    private ShareActionProvider mActionProvider;

    public static FragmentManager fragmentManager;

    private TripAdd tripAdd;
    private TripInfo tripInfo;
    private TripFragment tripList;
    private TripMap tripMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        tripList = (TripFragment) fragmentManager.getFragments().get(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void openTripInfo(Trip trip, Point point) {
        // open details
        tripInfo = tripInfo.newInstance(trip, point);

        ColorDrawable c = new ColorDrawable(trip.getColor());
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(trip.getColor() | 0xff000000));

        // close fragment
        fragmentManager.beginTransaction()
                .add(R.id.fragment, tripInfo)
                .addToBackStack(TripInfo.TAG)
                .commit();
    }

    @Override
    public void showAddFragment(Point point) {
        tripAdd = TripAdd.newInstance(point);

        fragmentManager.beginTransaction()
                .add(R.id.fragment, tripAdd)
                .addToBackStack(TripAdd.TAG)
                .commit();
    }

    @Override
    public void onAddTrip(Trip trip, Point point) {
        // signal trip add
        tripList.signal(trip);

        // open details
        tripInfo = TripInfo.newInstance(trip, point);

        // action bar
        ColorDrawable c = new ColorDrawable(trip.getColor());
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(trip.getColor() | 0xff000000));

        // close fragment
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction()
                .add(R.id.fragment, tripInfo)
                .addToBackStack(TripInfo.TAG)
                .commit();
    }

    @Override
    public void onInfoOpenMap(Trip trip, Point pos, boolean res, boolean bars, boolean culture) {
        tripMap = TripMap.newInstance(trip, pos , res, bars, culture);

        fragmentManager.beginTransaction()
                .add(R.id.fragment, tripMap)
                .addToBackStack(TripMap.TAG)
                .commit();
    }

    @Override
    public void onBackStackChanged() {
        Log.i("BACK", fragmentManager.getBackStackEntryCount() + "");
        if(fragmentManager.getBackStackEntryCount() == 0) {
            //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        else {
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                fragmentManager.popBackStackImmediate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
