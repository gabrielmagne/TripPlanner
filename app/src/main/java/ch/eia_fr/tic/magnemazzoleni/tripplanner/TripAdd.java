package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.FloatingActionButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.Trip;
import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.TripsSQL;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TripAdd.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripAdd#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripAdd extends Fragment implements OnMapReadyCallback {
    public static final String TAG = "ADD_TRIP";
    public static final String ARG_POS_X = "posX";
    public static final String ARG_POS_Y = "posY";

    private OnFragmentInteractionListener mListener;

    private GoogleMap mMap;
    private Marker departureMarker = null;
    private Marker arrivalMarker = null;
    private FloatingActionButton btnAdd;
    private int transX, transY;

    private String fromID = null;
    private String toID = null;

    private String departure = null;
    private String arrival = null;
    private LatLng latLngDep;
    private LatLng latLngArr;
    private int distance;
    private int duration;
    String title;

    private static AsyncHttpClient client = new AsyncHttpClient();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TripAdd.
     */
    public static TripAdd newInstance(Point pos) {
        TripAdd fragment = new TripAdd();
        Bundle args = new Bundle();
        args.putInt(ARG_POS_X, pos.x);
        args.putInt(ARG_POS_Y, pos.y);
        fragment.setArguments(args);
        return fragment;
    }

    public TripAdd() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transX = getArguments().getInt(ARG_POS_X);
            transY = getArguments().getInt(ARG_POS_Y);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_add, container, false);
        setUpMapIfNeeded();

        // set toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        btnAdd = (FloatingActionButton) view.findViewById(R.id.add_save);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddPressed();
            }
        });

        // lat lng position API
        final String posURI = "https://maps.googleapis.com/maps/api/place/details/json";

        // autocomplete for from
        final AutoCompleteTextView from = (AutoCompleteTextView) view.findViewById(R.id.add_from);
        final AutocompleteAdapter fromAdapter = new AutocompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
        from.setAdapter(fromAdapter);
        from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                JSONObject obj = fromAdapter.getItemJSON(position);
                hideSoftKeyboard();
                try {
                    departure = obj.getString("description");
                    fromID = obj.getString("place_id");
                    from.setText(departure);
                    // look for departure lat lng
                    RequestParams depParams = new RequestParams();
                    depParams.add("placeid", fromID);
                    depParams.add("key", getActivity().getResources().getString(R.string.google_server_key));
                    client.get(getActivity(), posURI, depParams, departureResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // autocomplete for to
        final AutoCompleteTextView to = (AutoCompleteTextView) view.findViewById(R.id.add_to);
        final AutocompleteAdapter toAdapter = new AutocompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
        to.setAdapter(toAdapter);
        to.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                JSONObject obj = toAdapter.getItemJSON(position);
                hideSoftKeyboard();
                try {
                    arrival = obj.getString("description");
                    toID = obj.getString("place_id");
                    to.setText(arrival);
                    // look for arrival lat lng
                    final RequestParams arrParams = new RequestParams();
                    arrParams.add("placeid", toID);
                    arrParams.add("key", getActivity().getResources().getString(R.string.google_server_key));
                    client.get(getActivity(), posURI, arrParams, arrivalResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

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

    public void onAddPressed() {

        if(fromID == null || toID == null) {
            Toast.makeText(getActivity(), "Please, select 2 locations", Toast.LENGTH_LONG).show();
            return;
        }

        // start asking google for more informations
        String disURI = "https://maps.googleapis.com/maps/api/distancematrix/json";
        RequestParams disParams = new RequestParams();

        disParams.add("origins", departure);
        disParams.add("destinations", arrival);
        // look for distance from target
        client.get(getActivity(), disURI, disParams, distanceResponse);
    }

    /**
     * When all three request are done, continue adding the trip
     */
    private void handleAdd() {
        TripsSQL sql = new TripsSQL(getActivity());

        Trip trip =  sql.insert(
            latLngDep,
            latLngArr,
            distance,
            duration,
            title,
            Utils.generateRandomColor(),
            departure,
            arrival
        );

        if (mListener != null) {
            int[] pos = new int[2];
            int w = btnAdd.getWidth() / 2;
            btnAdd.getLocationOnScreen(pos);
            Point ppos = new Point(pos[0] + w, pos[1] + w);
            mListener.onAddTrip(trip, ppos);
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

    private void hideSoftKeyboard() {
        // hide soft keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hideSoftKeyboard();
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
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_add);
            supportMapFragment.getMapAsync(this);
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            if(departureMarker == null) {
                MarkerOptions optsDep = new MarkerOptions();
                optsDep.position(new LatLng(0, 0));
                optsDep.visible(false);
                departureMarker = mMap.addMarker(optsDep);
            }
            if(arrivalMarker == null) {
                MarkerOptions optsArr = new MarkerOptions();
                optsArr.position(new LatLng(0, 0));
                optsArr.visible(false);
                arrivalMarker = mMap.addMarker(optsArr);
            }
        }
    }

    private void moveDepartureMarker() {
        departureMarker.setPosition(latLngDep);
        departureMarker.setVisible(true);
        positionViewPort();
    }

    private void moveArrivalMarker() {
        arrivalMarker.setPosition(latLngArr);
        arrivalMarker.setVisible(true);
        positionViewPort();
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
        public void onAddTrip(Trip trip, Point point);
    }

    // Async task objects
    /**
     * Departure Lat Lng response handler
     */
    JsonHttpResponseHandler departureResponse = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                JSONObject location = response.getJSONObject("result")
                        .getJSONObject("geometry")
                        .getJSONObject("location");
                latLngDep = new LatLng(
                        location.getDouble("lat"),
                        location.getDouble("lng")
                );

                moveDepartureMarker();
            } catch (JSONException e) {
                sendRetryMessage(RETRY_MESSAGE);
            }
        }
    };

    /**
     * Arrival Lat Lng response handler
     */
    JsonHttpResponseHandler arrivalResponse = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                JSONObject location = response.getJSONObject("result")
                        .getJSONObject("geometry")
                        .getJSONObject("location");
                latLngArr = new LatLng(
                        location.getDouble("lat"),
                        location.getDouble("lng")
                );
                moveArrivalMarker();
            } catch (JSONException e) {
                sendRetryMessage(RETRY_MESSAGE);
            }
        }
    };

    /**
     * Distance from point A to point B
     */
    JsonHttpResponseHandler distanceResponse = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                JSONObject values = response.getJSONArray("rows")
                        .getJSONObject(0)
                        .getJSONArray("elements")
                        .getJSONObject(0);

                String status = values.getString("status");
                if(!status.equals("OK")) {
                    Toast.makeText(getActivity(), "No route found, retry", Toast.LENGTH_LONG).show();
                    return;
                }

                distance = values.getJSONObject("distance").getInt("value");
                duration = values.getJSONObject("duration").getInt("value");

                openNameDialog();
            } catch (JSONException e) {
                sendRetryMessage(RETRY_MESSAGE);
            }
        }
    };

    private void openNameDialog() {
        final SimpleDialog dialog = new SimpleDialog(getActivity());
        dialog.title("Trip Title")
              .positiveAction("ADD")
              .negativeAction("CANCEL")
              .contentView(R.layout.add_dialog)
              .cancelable(true)
              .show();

        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText) dialog.findViewById(R.id.add_dialog_text);
                String title = text.getText().toString().trim();

                if (title.equals("")) {
                    Toast.makeText(getActivity(), "A title is needed", Toast.LENGTH_SHORT).show();
                    return;
                }

                TripAdd.this.title = title;

                dialog.dismiss();
                handleAdd();
            }
        });

        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void positionViewPort() {
        if(latLngDep != null && latLngArr == null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngDep));
            return;
        }
        if(latLngArr != null && latLngDep == null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngArr));
            return;
        }
        double dLat = latLngDep.latitude  + 90;
        double dLng = latLngDep.longitude + 180;
        double aLat = latLngArr.latitude  + 90;
        double aLng = latLngArr.longitude + 180;

        double n = Math.max(dLat, aLat) - 90;
        double s = Math.min(dLat, aLat) - 90;
        double e = Math.max(dLng, aLng) - 180;
        double w = Math.min(dLng, aLng) - 180;
        LatLng ne = new LatLng(n, e);
        LatLng sw = new LatLng(s, w);

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(sw, ne), 200));
    }
}
