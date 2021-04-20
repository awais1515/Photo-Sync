package com.example.filesynchor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filesynchor.Database.DataRepo;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BottomSheet extends BottomSheetDialogFragment {

    RecyclerView recyclerView;
    AdapterSync adapterSync;
    private List<Data> dataList;
    private TextView tvNoSyncHistoryFound;
    ImageView btnDeleteAll,btnSyncAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, null);
        dataList = DataRepo.getDataList();
        adapterSync = new AdapterSync(dataList, getActivity());
        btnDeleteAll = view.findViewById(R.id.btn_deleteAll);
        btnSyncAll = view.findViewById(R.id.btn_synAll);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvNoSyncHistoryFound = view.findViewById(R.id.tv_sync_not_found);
        if(dataList.size()>0)
            tvNoSyncHistoryFound.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapterSync);
        setListeners();
        return view;
    }
    private void setListeners(){
        btnSyncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareAllFilesToLightRoom();
            }
        });
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAllDialog();
            }
        });
    }

    private void showDeleteAllDialog() {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setMessage("Do you want to delete all history?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DataRepo.deleteAllData();
                tvNoSyncHistoryFound.setVisibility(View.VISIBLE);
                dataList.clear();
                adapterSync.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();

                    }
                });
        dialog = builder.create();
        dialog.show();
    }


    private void shareAllFilesToLightRoom(){
        File destinationFolder = new File(SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,SharedPref.DEFAULT_DESTINATION_FOLDER));
        Log.d("abc",destinationFolder.getAbsolutePath());
        if(destinationFolder.exists()){
            Log.d("abc",destinationFolder.getAbsolutePath());
            final ArrayList<String> syncedFiles = getFilesReadyToShare();
            if(syncedFiles.size()>0){
                Dialog dialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        getActivity());
                builder.setMessage("Do you want to share all "+syncedFiles.size()+" files to Light Room?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "CR3 files.");
                        intent.setType("image/jpeg"); /* This example is sharing jpeg images. */
                        intent.setPackage("com.adobe.lrmobile");
                        ArrayList<Uri> files = new ArrayList<Uri>();
                        for(String path : syncedFiles /* List of the files you want to send */) {
                            File file = new File(path);
                            if(file.exists()){
                                Uri uri = FileProvider.getUriForFile(getActivity(), "com.example.filesynchor.provider",file);
                                files.add(uri);
                            }
                        }
                        Log.d("abc","no of files "+files.size()+"");
                        Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.adobe.lrmobile");
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                        getActivity().startActivities(new Intent[]{launchIntent,intent});
                    }
                });

                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();

                            }
                        });
                dialog = builder.create();
                dialog.show();
            }
            else {
                App.showToast("No files available to Share with Light Room");
            }

        }
        else {
            Toast.makeText(getActivity(),"Destination Folder doesn't exist",Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList<String> getFilesReadyToShare(){
        ArrayList<String> filePathsList = new ArrayList<>();
        for(Data data:dataList){
            String filePaths = data.getPaths();
            if(!filePaths.trim().equals("")){
                String files[] = filePaths.split("\n");
                for(String file:files){
                    if(new File(file).exists()){
                        filePathsList.add(file);
                        Log.d("abc","Path: "+file);
                    }
                }
            }
        }
        return filePathsList;
    }


}