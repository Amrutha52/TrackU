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
import com.happy.tracku.gson.updatedeliverystatus.DeliveryStatusUpdate;
import com.happy.tracku.viewholders.PhotoPunchHistoryViewHolder;

import java.util.List;


public class DeliveryReportAdapters extends RecyclerView.Adapter<PhotoPunchHistoryViewHolder>
{

    List<DeliveryStatusUpdate> getDeliveryReportList;
    Context context;

    public DeliveryReportAdapters(Context context, List<DeliveryStatusUpdate> getDeliveryReportList)
    {
        this.context = context;
        this.getDeliveryReportList = getDeliveryReportList;
    }

    @NonNull
    @Override
    public PhotoPunchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_photo_punch_history_adapters, parent, false);
        return new PhotoPunchHistoryViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull PhotoPunchHistoryViewHolder holder, int position)
    {

        Log.e("Log", "emi position" + position);

        if (position == 0)
        {
            holder.punchDateTV.setText("Date");
            holder.punchDateTV.setTypeface(Typeface.DEFAULT_BOLD);
            //holder.punchDateTV.setBackgroundColor(ContextCompat.getColor(holder.punchDateTV.getContext(), R.color.purple_200));
            holder.employeeCodeTV.setText("Employee");
            holder.employeeCodeTV.setTypeface(Typeface.DEFAULT_BOLD);
            holder.punchINTV.setText("IN Time");
            holder.punchDateTV.setTypeface(Typeface.DEFAULT_BOLD);
            //holder.punchINTV.setBackgroundColor(ContextCompat.getColor(holder.punchINTV.getContext(), R.color.purple_200));

        }
        else{

            GetPunchHistoryDetail getPunchHistoryDetail = getPunchHistoryDetailList.get(position-1);

            holder.punchDateTV.setText(getPunchHistoryDetail.getDate());
            holder.punchDateTV.setTypeface(Typeface.DEFAULT_BOLD);
            // holder.punchDateTV.setBackgroundColor(ContextCompat.getColor(holder.punchDateTV.getContext(), R.color.white));

            holder.employeeCodeTV.setText(getPunchHistoryDetail.getEmployeeCode() + '-' + getPunchHistoryDetail.getEmployeeName());
            holder.employeeCodeTV.setTypeface(Typeface.DEFAULT_BOLD);

            holder.punchINTV.setText(getPunchHistoryDetail.getPunchTime());
            holder.punchDateTV.setTypeface(Typeface.DEFAULT_BOLD);
            //holder.punchINTV.setBackgroundColor(ContextCompat.getColor(holder.punchINTV.getContext(), R.color.white));

        }
    }

    @Override
    public int getItemCount()
    {
        return getPunchHistoryDetailList.size()+1;
    }
}


