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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
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
    private OnFragmentInteractionListener mListener;

    private GoogleMap mMap;

    private String fromID = null;
    private String toID = null;
    private boolean loading = false;

    private String departure = null;
    private String arrival = null;
    private LatLng latLngDep;
    private LatLng latLngArr;
    private int distance;
    private int duration;

    private static AsyncHttpClient client = new AsyncHttpClient();

    private int successfulResponses = 0;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TripAdd.
     */
    public static TripAdd newInstance() {
        TripAdd fragment = new TripAdd();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TripAdd() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_add, container, false);
        setUpMapIfNeeded();

        ButtonFloat btn = (ButtonFloat) view.findViewById(R.id.add_save);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddPressed();
            }
        });

        // autocomplete for from
        final AutoCompleteTextView from = (AutoCompleteTextView) view.findViewById(R.id.add_from);
        final AutocompleteAdapter fromAdapter = new AutocompleteAdapter(getActivity(), android.R.layout.simple_list_item_1);
        from.setAdapter(fromAdapter);
        from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                JSONObject obj = fromAdapter.getItemJSON(position);
                try {
                    departure = obj.getString("description");
                    fromID = obj.getString("place_id");
                    from.setText(departure);
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
                try {
                    arrival = obj.getString("description");
                    toID = obj.getString("place_id");
                    to.setText(arrival);
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
            int cx = size.x / 2;
            int cy = size.y / 2;
            int finalRadius = Math.max(size.x, size.y) / 2;
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            view.setVisibility(View.VISIBLE);
            anim.start();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setEnterTransition(Object transition) {
        super.setEnterTransition(transition);
    }

    public void onAddPressed() {

        if(loading) return;

        if(fromID == null || toID == null) {
            Toast.makeText(getActivity(), "Please, select 2 locations", Toast.LENGTH_LONG);
            return;
        }

        loading = true;

        // start asking google for more informations
        String posURI = "https://maps.googleapis.com/maps/api/place/details/json";
        String disURI = "https://maps.googleapis.com/maps/api/distancematrix/json";
        RequestParams depParams = new RequestParams();
        RequestParams arrParams = new RequestParams();
        RequestParams disParams = new RequestParams();

        depParams.add("placeid", fromID);
        depParams.add("key", getActivity().getResources().getString(R.string.google_server_key));
        arrParams.add("placeid", toID);
        arrParams.add("key", getActivity().getResources().getString(R.string.google_server_key));

        disParams.add("origins", departure);
        disParams.add("destinations", arrival);

        // look for departure lat lng
        client.get(getActivity(), posURI, depParams, departureResponse);
        // look for arrival lat lng
        client.get(getActivity(), posURI, arrParams, arrivalResponse);
        // look for distance from target
        client.get(getActivity(), disURI, disParams, distanceResponse);
    }

    /**
     * When all three request are done, continue adding the trip
     */
    private void handleAdd() {
        if(++successfulResponses < 3) return;

        Log.i("TripAdd", "All params get");

        TripsSQL sql = new TripsSQL(getActivity());

        // TODO: the name of the trip should be specified somewhere
        Trip trip =  sql.insert(
            latLngDep,
            latLngArr,
            distance / 1000d,
            "" + Math.random(),
            generateRandomColor(),
            departure,
            arrival
        );

        if (mListener != null) {
            mListener.onAddTrip(trip);
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

    /**
     * Generate a random color
     * @return
     */
    private int generateRandomColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        red = (red + 255)     / 2;
        green = (green + 255) / 2;
        blue = (blue + 255)   / 2;

        return red << 8 | green << 4 | blue;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // hide soft keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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

    private void setUpMap() {
        MarkerOptions mo = new MarkerOptions().position(new LatLng(0, 0)).title("Marker").draggable(true);
        mMap.addMarker(mo);
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
        public void onAddTrip(Trip trip);
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

                handleAdd();
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

                handleAdd();
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

                distance = values.getJSONObject("distance").getInt("value");
                duration = values.getJSONObject("duration").getInt("value");

                handleAdd();
            } catch (JSONException e) {
                sendRetryMessage(RETRY_MESSAGE);
            }
        }
    };
}
