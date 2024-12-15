package com.happy.tracku.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.happy.tracku.R;
import com.happy.tracku.gson.photopunchhistoryjson.GetPunchHistoryDetail;
import com.happy.tracku.gson.travellogdetails.TravelLogDetail;
import com.happy.tracku.viewholders.PhotoPunchHistoryViewHolder;
import com.happy.tracku.viewholders.TravelLogViewHolder;

import java.util.List;


public class TravelLogDetailsAdapter extends RecyclerView.Adapter<TravelLogViewHolder>
{

    List<TravelLogDetail> travelLogDetailList;
    Context context;

    public TravelLogDetailsAdapter(Context context, List<TravelLogDetail> travelLogDetailList)
    {
        this.context = context;
        this.travelLogDetailList = travelLogDetailList;
    }

    @NonNull
    @Override
    public TravelLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_travel_log_adapter, parent, false);
        return new TravelLogViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull TravelLogViewHolder holder, int position)
    {

        Log.e("Log", "emi position" + position);

        if (position == 0)
        {
            holder.travelDateTV.setText("Date");
            holder.travelDateTV.setTypeface(Typeface.DEFAULT_BOLD);
            //holder.punchDateTV.setBackgroundColor(ContextCompat.getColor(holder.punchDateTV.getContext(), R.color.purple_200));
            holder.travelTimeTV.setText("Time");
            holder.travelTimeTV.setTypeface(Typeface.DEFAULT_BOLD);
            holder.addressTV.setText("Address");
            holder.addressTV.setTypeface(Typeface.DEFAULT_BOLD);
            //holder.punchINTV.setBackgroundColor(ContextCompat.getColor(holder.punchINTV.getContext(), R.color.purple_200));

        }
        else{

            TravelLogDetail travelLogDetail = travelLogDetailList.get(position-1);

            holder.travelDateTV.setText(travelLogDetail.getDate());
            holder.travelDateTV.setTypeface(Typeface.DEFAULT_BOLD);
            // holder.punchDateTV.setBackgroundColor(ContextCompat.getColor(holder.punchDateTV.getContext(), R.color.white));

            holder.travelTimeTV.setText(travelLogDetail.getTime());
            holder.travelTimeTV.setTypeface(Typeface.DEFAULT_BOLD);
            //holder.punchINTV.setBackgroundColor(ContextCompat.getColor(holder.punchINTV.getContext(), R.color.white));

            holder.addressTV.setText(travelLogDetail.getAddress());
            holder.addressTV.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    @Override
    public int getItemCount()
    {
        return travelLogDetailList.size()+1;
    }
}