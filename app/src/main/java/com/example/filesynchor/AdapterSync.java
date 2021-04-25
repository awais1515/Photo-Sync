package com.example.filesynchor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filesynchor.Database.DataRepo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdapterSync extends RecyclerView.Adapter<AdapterSync.MyViewHolder> {
    Context context;
    private List<Data> dataList;
    public AdapterSync(List<Data> dataList,Context context){
        this.context = context;
        this.dataList=dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sync,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        holder.setIsRecyclable(false);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final Data data=dataList.get(position);
        holder.syncTime.setText(data.getSyncTime());
        holder.syncedFiles.setText(data.getSyncedFiles()+"");
        holder.syncSpeed.setText(data.getStatus());
        holder.syncedGB.setText(data.getSyncedGB());
        if(data.getSyncedFiles()<=0)
        {
            holder.shareButton.setEnabled(false);
            holder.shareButton.setBackground(context.getDrawable(R.drawable.rec_shape_disabled));
        }
        else {
            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // shareToLightRoom(data.getPaths());
                    App.showToast("Share to light room is disabled temporarily");
                }
            });
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDeleteDialog(data,position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
    TextView syncTime,syncedFiles,syncSpeed,syncedGB;
    Button shareButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            syncTime=(TextView) itemView.findViewById(R.id.tvLastSyncTime);
            syncedFiles=(TextView) itemView.findViewById(R.id.tvNoOfFilesSynced);
            syncSpeed=(TextView) itemView.findViewById(R.id.tv_sync_speed);
            syncedGB=(TextView) itemView.findViewById(R.id.tvSyncedDataAmount);
            shareButton = itemView.findViewById(R.id.btnShareToLightRoom);
        }
    }


    private void showDeleteDialog(final Data data, final int position)
    {
        AlertDialog alertDialog =new AlertDialog.Builder(context)
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this sync item")


                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        DataRepo.deleteData(data);
                        dataList.remove(position);
                        notifyDataSetChanged();
                        App.showToast("Deleted Item Successfully");

                    }
                })

                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        dialog.dismiss();
                    }
                })

                .create();
        alertDialog.show();

    }




    private ArrayList<String> getFilesReadyToShare(String filePaths){
        if(filePaths.equals(""))
            return new ArrayList<String>();
        String files[] = filePaths.split("\n");
        ArrayList<String> filePathsList = new ArrayList<>();
        for(String file:files){
            if(new File(file).exists()){
                filePathsList.add(file);
                Log.d("abc","Path: "+file);
            }
        }
        return filePathsList;
    }

    private void shareToLightRoom(String filePaths){
        File destinationFolder = new File(SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,SharedPref.DEFAULT_DESTINATION_FOLDER));
        Log.d("abc",destinationFolder.getAbsolutePath());
        if(destinationFolder.exists()){
            // File destinationFolder = new File(tvFolderPath.getText().toString());
            Log.d("abc",destinationFolder.getAbsolutePath());
            ArrayList<String> syncedFiles = getFilesReadyToShare(filePaths);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putExtra(Intent.EXTRA_SUBJECT, "CR3 files.");
            intent.setType("image/jpeg"); /* This example is sharing jpeg images. */
            intent.setPackage("com.adobe.lrmobile");

            ArrayList<Uri> files = new ArrayList<Uri>();

            for(String path : syncedFiles /* List of the files you want to send */) {
                File file = new File(path);
                if(file.exists()){
                    Uri uri = FileProvider.getUriForFile(context, "com.example.filesynchor.provider",file);
                    files.add(uri);
                }

            }
            Log.d("abc","no of files "+files.size()+"");

            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.adobe.lrmobile");
            //startActivity( launchIntent );

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            // SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"");
            // startActivity(intent);
            context.startActivities(new Intent[]{launchIntent,intent});

            //startActivity(Intent.createChooser(intent, "Share the Pictures"));
        }
        else {
            Toast.makeText(context,"Destination Folder doesn't exist",Toast.LENGTH_LONG).show();
        }




    }

}
