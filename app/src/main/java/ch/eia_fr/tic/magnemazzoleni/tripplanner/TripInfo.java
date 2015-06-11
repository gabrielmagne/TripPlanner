package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.Trip;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TripInfo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TripInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TripInfo extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TRIP = "trip";

    private Trip trip;

    private TextView tripname;
    private TextView departure;
    private TextView arrival;
    private TextView km;
    private TextView duration;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param trip Parameter 1.
     * @return A new instance of fragment TripInfo.
     */
    public static TripInfo newInstance(Trip trip) {
        TripInfo fragment = new TripInfo();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRIP, trip);
        fragment.setArguments(args);
        return fragment;
    }

    public TripInfo() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trip = (Trip) getArguments().getSerializable(ARG_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip_info, container, false);

        // background
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.fragment_trip_info);
        layout.setBackground(new ColorDrawable(Utils.lighterColor(trip.getColor()) | 0xff000000));

        // elements
        tripname = (TextView) view.findViewById(R.id.info_tripname);
        departure = (TextView) view.findViewById(R.id.info_departure);
        arrival = (TextView) view.findViewById(R.id.info_arrival);
        km = (TextView) view.findViewById(R.id.info_km);
        duration = (TextView) view.findViewById(R.id.info_time);

        // fill with trip
        tripname.setText(trip.getName());
        departure.setText(trip.getDepartureAddress());
        arrival.setText(trip.getArrivalAddress());
        if(trip.getDistance() > 1000)
            km.setText(String.format("%.2f km", trip.getDistance() / 1000d));
        else
            km.setText(String.format("%d m", trip.getDistance()));
        duration.setText(timeString(trip.getDuration()));

        // Inflate the layout for this fragment
        return view;
    }

    private String timeString(int time) {
        int h = (time / (60 * 60));
        int m = (time / (60 )) % 60;
        String out = "";
        if(h > 0)
            out += "%1$d h ";
        if(m > 0)
            out += "%2$d min";
        return String.format(out, h, m);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonMapOpen(Trip trip) {
        if (mListener != null) {
            mListener.onInfoOpenMap(trip);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            int cx = size.x / 2;
            int cy = size.y / 2;
            int finalRadius = (int) (Math.max(size.x, size.y) / 1.6d);
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
        public void onInfoOpenMap(Trip trip);
    }

}
