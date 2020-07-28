package com.example.filesynchor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdapterSync extends RecyclerView.Adapter<AdapterSync.MyViewHolder> {
    Context context;
    private List<Data> dataList1;
    public AdapterSync(List<Data> dataList,Context context){
        this.context = context;
        this.dataList1=dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sync,parent,false);
        MyViewHolder myViewHolder= new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Data data=dataList1.get(position);
        holder.syncTime.setText(data.getSyncTime());
        holder.syncedFiles.setText(data.getSyncedFiles()+"");
        holder.syncSpeed.setText(data.getStatus());
        holder.syncedGB.setText(data.getSyncedGB());


    }

    @Override
    public int getItemCount() {
        return dataList1.size();
    }
    public  static class MyViewHolder extends RecyclerView.ViewHolder{
    TextView syncTime,syncedFiles,syncSpeed,syncedGB;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            syncTime=(TextView) itemView.findViewById(R.id.tvLastSyncTime);
            syncedFiles=(TextView) itemView.findViewById(R.id.tvNoOfFilesSynced);
            syncSpeed=(TextView) itemView.findViewById(R.id.tv_sync_speed);
            syncedGB=(TextView) itemView.findViewById(R.id.tvSyncedDataAmount);

        }

    }











}
