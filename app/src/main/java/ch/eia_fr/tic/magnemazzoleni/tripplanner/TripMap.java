package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.Trip;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TripMap.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripMap#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripMap extends Fragment implements OnMapReadyCallback {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TRIP = "trip";
    private static final String ARG_POS_X = "posX";
    private static final String ARG_POS_Y = "posY";

    public static final String TAG = "MAPS";

    private GoogleMap mMap;
    private int transX, transY;

    private Trip trip;

    private OnFragmentInteractionListener mListener;
    private static AsyncHttpClient client = new AsyncHttpClient();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param pos Start animation point.
     * @return A new instance of fragment TripMap.
     */
    // TODO: Rename and change types and number of parameters
    public static TripMap newInstance(Trip trip, Point pos) {
        TripMap fragment = new TripMap();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRIP, trip);
        args.putInt(ARG_POS_X, pos.x);
        args.putInt(ARG_POS_Y, pos.y);
        fragment.setArguments(args);
        return fragment;
    }

    public TripMap() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trip = (Trip) getArguments().getSerializable(ARG_TRIP);
            transX = getArguments().getInt(ARG_POS_X);
            transY = getArguments().getInt(ARG_POS_Y);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_trip_map, container, false);
        setUpMapIfNeeded();

        // set toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        String posURI = "https://maps.googleapis.com/maps/api/directions/json";
        RequestParams params = new RequestParams();
        params.add("origin", String.format("%f,%f", trip.getDeparture().latitude, trip.getDeparture().longitude));
        params.add("destination", String.format("%f,%f", trip.getArrival().latitude, trip.getArrival().longitude));

        client.get(getActivity(), posURI, params, directions);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            int cx = transX;
            int cy = transY;
            int finalRadius = Math.max(size.x, size.y);
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            view.setVisibility(View.VISIBLE);
            anim.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition((ViewGroup) getView().getRootView(), new Fade());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_map);
            supportMapFragment.getMapAsync(this);
        }
    }

    private void setUpMap() {
        MarkerOptions dep = new MarkerOptions().position(trip.getDeparture()).title(trip.getDepartureAddress()).draggable(false);
        MarkerOptions arr = new MarkerOptions().position(trip.getArrival()).title(trip.getArrivalAddress()).draggable(false);
        mMap.addMarker(dep);
        mMap.addMarker(arr);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            setUpMap();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
    }

    private JsonHttpResponseHandler directions = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);

            try {
                JSONObject one = response.getJSONArray("routes")
                                         .getJSONObject(0)
                                         .getJSONObject("bounds")
                                         .getJSONObject("northeast");
                JSONObject osw = response.getJSONArray("routes")
                                         .getJSONObject(0)
                                         .getJSONObject("bounds")
                                         .getJSONObject("southwest");

                LatLng ne = new LatLng(one.getDouble("lat"), one.getDouble("lng"));
                LatLng sw = new LatLng(osw.getDouble("lat"), osw.getDouble("lng"));

                LatLngBounds bounds = new LatLngBounds(sw, ne);

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

                JSONArray values = response.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONArray("legs")
                        .getJSONObject(0)
                        .getJSONArray("steps");

                List<LatLng> locations = new ArrayList<>();

                for(int i = 0; i < values.length(); i++) {
                    locations.addAll(
                            PolylineUtils.decode(
                                    values.getJSONObject(i)
                                          .getJSONObject("polyline")
                                          .getString("points")
                            )
                    );
                }

                PolylineOptions options = new PolylineOptions();

                options.color(Color.BLUE);
                options.width(7.5f);
                options.visible(true);

                options.addAll(locations);

                mMap.addPolyline(options);
            } catch (JSONException e) {
                sendRetryMessage(RETRY_MESSAGE);
            }
        }
    };
}
