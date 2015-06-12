package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.gc.materialdesign.views.CustomView;

import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.Trip;

public class TripActivity extends ActionBarActivity
        implements TripFragment.OnFragmentInteractionListener,
                     TripAdd.OnFragmentInteractionListener,
                     TripInfo.OnFragmentInteractionListener,
                     FragmentManager.OnBackStackChangedListener {

    private ShareActionProvider mActionProvider;

    public static FragmentManager fragmentManager;

    private TripAdd tripAdd;
    private TripInfo tripInfo;
    private TripFragment tripList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        getSupportActionBar().setCustomView(new CustomView(this, null));
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);
        // whitr bg
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        // back text
        Spannable text = new SpannableString(getSupportActionBar().getTitle());
        text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        getSupportActionBar().setTitle(text);

        tripList = (TripFragment) fragmentManager.getFragments().get(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void openTripInfo(Trip trip) {
        // open details
        tripInfo = tripInfo.newInstance(trip);

        ColorDrawable c = new ColorDrawable(trip.getColor());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(trip.getColor() | 0xff000000));

        // close fragment
        fragmentManager.beginTransaction()
                .add(R.id.fragment, tripInfo)
                .addToBackStack(TripInfo.TAG)
                .commit();
    }

    @Override
    public void showAddFragment() {
        tripAdd = TripAdd.newInstance();

        fragmentManager.beginTransaction()
                .add(R.id.fragment, tripAdd)
                .addToBackStack(TripAdd.TAG)
                .commit();
    }

    @Override
    public void onAddTrip(Trip trip) {
        // signal trip add
        tripList.signal(trip);

        // open details
        tripInfo = TripInfo.newInstance(trip);

        // action bar
        ColorDrawable c = new ColorDrawable(trip.getColor());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(trip.getColor() | 0xff000000));

        // close fragment
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction()
                .add(R.id.fragment, tripInfo)
                .addToBackStack(TripInfo.TAG)
                .commit();
    }

    @Override
    public void onInfoOpenMap(Trip trip) {
        Log.i("Evt Info", trip.getId() + "");
    }

    @Override
    public void onBackStackChanged() {
        Log.i("BACK", fragmentManager.getBackStackEntryCount() + "");
        if(fragmentManager.getBackStackEntryCount() == 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                View view = getSupportActionBar().getCustomView();
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                Animator anim = ViewAnimationUtils.createCircularReveal(view, 0, 0, 0, 1000);

                anim.start();
            }
        }
        else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
