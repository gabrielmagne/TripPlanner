package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.SnackBar;

import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.Trip;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class TripFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private FloatingActionButton btnAdd;

    /**
     * The fragment's ListView/GridView.
     */
    private RecyclerView recyclerView;
    private TripAdapter tripAdapter;

    public static TripFragment newInstance() {
        TripFragment fragment = new TripFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TripFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trip, container, false);

        // set toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        // floating button
        btnAdd = (FloatingActionButton) view.findViewById(R.id.list_add_trip);

        // tell activity to switch fragment
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] pos = new int[2];
                int w = btnAdd.getWidth() / 2;
                btnAdd.getLocationOnScreen(pos);
                Point ppos = new Point(pos[0] + w, pos[1] + w);
                mListener.showAddFragment(ppos);
            }
        });

        // start recycle view
        recyclerView = (RecyclerView) view.findViewById(R.id.list_cards);
        tripAdapter = new TripAdapter(getActivity(), mListener);
        // setup
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(tripAdapter);

        // init swipe to dismiss logic
        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // callback for swipe to dismiss, removing item from data and adapter
                final int id = viewHolder.getAdapterPosition();
                final Trip deleted = tripAdapter.remove(id);
                final boolean[] canRemove = {true};
                Snackbar snackbar = Snackbar.make(getView(), "DONE FOR NOW", Snackbar.LENGTH_LONG)
                        .setActionTextColor(btnAdd.getBackgroundColor())
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                canRemove[0] = false;
                                tripAdapter.add(deleted, id);
                            }
                        });
                snackbar.show();

                // XXX: Hackish
                new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (canRemove[0]) {
                            tripAdapter.remove(deleted);
                        }
                    }
                }, 3500);
            }
        });
        swipeToDismissTouchHelper.attachToRecyclerView(recyclerView);

        return view;
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

    public void signal(Trip trip) {
        tripAdapter.add(trip);
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
        public void openTripInfo(Trip trip, Point point);
        public void showAddFragment(Point point);
    }

}
