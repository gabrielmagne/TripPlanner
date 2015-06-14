package ch.eia_fr.tic.magnemazzoleni.tripplanner;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.Trip;
import ch.eia_fr.tic.magnemazzoleni.tripplanner.sql.TripsSQL;

/**
 * Created by Dosky on 13.06.2015.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> tripList;
    private TripFragment.OnFragmentInteractionListener callback;
    private TripsSQL sql;

    public TripAdapter(Context context, TripFragment.OnFragmentInteractionListener callback) {
        this.callback = callback;

        sql = new TripsSQL(context);
        tripList = sql.getAll();
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // create view
        View itemView = LayoutInflater.from(viewGroup.getContext())
                                      .inflate(R.layout.recycle_list, viewGroup, false);

        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TripViewHolder tripViewHolder, int i) {
        // item
        final Trip trip = tripList.get(i);
        // set view
        tripViewHolder.name.setText(trip.getName());
        tripViewHolder.from.setText(trip.getDepartureAddress());
        tripViewHolder.to.setText(trip.getArrivalAddress());
        tripViewHolder.color.setBackground(new ColorDrawable(trip.getColor() | 0xff000000));

        // bind event
        tripViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int[] pos = new int[2];
                int h = tripViewHolder.color.getHeight() / 2;
                tripViewHolder.color.getLocationOnScreen(pos);
                Point ppoint = new Point(pos[0], pos[1] + h);
                callback.openTripInfo(trip, ppoint);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public void add(Trip trip) {
        add(trip, 0);
    }

    public void add(Trip trip, int position) {
        tripList.add(position, trip);
        this.notifyDataSetChanged();
    }

    public Trip remove(int position) {
        Trip del = tripList.get(position);
        tripList.remove(position);
        this.notifyDataSetChanged();
        return del;
    }

    public void remove(Trip deleted) {
        sql.delete(deleted);
    }

    protected class TripViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView from;
        private TextView to;
        private View color;
        private View container;

        public TripViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.recycle_name);
            from = (TextView) itemView.findViewById(R.id.recycle_from);
            to = (TextView) itemView.findViewById(R.id.recycle_to);
            color = itemView.findViewById(R.id.recycle_color);
            container = itemView;
        }
    }
}
