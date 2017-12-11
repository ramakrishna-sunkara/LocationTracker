package com.vysh.locationtracker.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vysh.locationtracker.R;
import com.vysh.locationtracker.TripModel;

import java.util.List;

/**
 * Created by ramakrishna on 12/2/17.
 */

public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.MyViewHolder> {

    private List<TripModel> tripModels;
    private TripClickListener tripClickListener;
    private Context context;

    public interface TripClickListener {
        void onTripItemClick(TripModel tripModel);
        void deleteTripItem(TripModel tripModel);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtStartDate, txtEndDate, txtTotalPoints;
        public CardView cvItem;
        public ImageView imgDelete;

        public MyViewHolder(View view) {
            super(view);
            txtStartDate = (TextView) view.findViewById(R.id.txtStartDate);
            txtEndDate = (TextView) view.findViewById(R.id.txtEndDate);
            txtTotalPoints = (TextView) view.findViewById(R.id.txtTotalPoints);
            cvItem = (CardView) view.findViewById(R.id.cvItem);
            imgDelete = (ImageView) view.findViewById(R.id.imgDelete);
        }
    }

    public void setTripClickListener(TripClickListener tripClickListener) {
        this.tripClickListener = tripClickListener;
    }


    public TripListAdapter(List<TripModel> tripModels) {
        this.tripModels = tripModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final TripModel tripModel = tripModels.get(position);
        holder.txtStartDate.setText(context.getString(R.string.label_start_date, tripModel.getTripStartDate()));
        holder.txtEndDate.setText(context.getString(R.string.label_end_date, tripModel.getTripEndDate()));
        holder.txtTotalPoints.setText(context.getString(R.string.label_trip_points,
                tripModel.getTripLocationPoints() == null ? "0" : String.valueOf(tripModel.getTripLocationPoints().size())));

        holder.cvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tripClickListener != null) {
                    tripClickListener.onTripItemClick(tripModel);
                }
            }
        });

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tripClickListener != null) {
                    tripClickListener.deleteTripItem(tripModel);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return tripModels.size();
    }
}
