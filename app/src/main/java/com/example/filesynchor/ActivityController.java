package com.example.filesynchor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.io.File;
import java.util.ArrayList;

import static com.example.filesynchor.App.TAG;
import static com.example.filesynchor.BottomSheet.*;

public class ActivityController extends AppCompatActivity {
    private static final int REQUEST_DIRECTORY = 4;
    RelativeLayout rlProgressPanel;
    TextView
            tvLastSyncStatus,tvLastSyncTime,tvNoOFilesSynced,tvNoOFilesSkipped,
            tvSyncedDataAmount,tvFolderPath,tvCopiedFilesCount,tvPercentage,tvCopying,
            tvCompleted,tvSyncDuration,tvSyncSpeed,tvReadyToShare;
    Button btnManualSync,btnAutoSync,btnChooseFolder,btnShare;
    ImageView btnClear;
    ProgressBar progressBar;
    Intent serviceIntent;
    BroadcastReceiver localBroadCast;
    ProgressDialog progressDialog;
    Button btnHistory;
    BottomSheet bottomSheetDialog;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        init();
        clickListeners();
       /* btnClear =(ImageView)findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              bottomSheetDialog.dismiss();
            }
        });*/

        btnHistory= findViewById(R.id.btn_SyncHistory);

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    public void showDialog() {
        bottomSheetDialog = new BottomSheet();
        bottomSheetDialog.show(getSupportFragmentManager(),"dialog");

    }

    private void init(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Syncing Data");
        //shared preferences items for last sync
        tvLastSyncStatus = findViewById(R.id.tv_status);
        tvLastSyncTime = findViewById(R.id.tvLastSyncTime);
        tvNoOFilesSynced = findViewById(R.id.tvNoOfFilesSynced);
        tvNoOFilesSkipped = findViewById(R.id.tvNoOfSkippedFiles);
        tvSyncedDataAmount = findViewById(R.id.tvSyncedDataAmount);
        tvReadyToShare = findViewById(R.id.tvReadyToShare);
        //textView for folder path
        tvFolderPath = findViewById(R.id.tvFolder);
        //buttons for sync
        btnAutoSync = findViewById(R.id.btn_auto_sync);
        btnManualSync = findViewById(R.id.btn_manual_sync);
        //button for choose folder
        btnChooseFolder = findViewById(R.id.btn_choose_folder);
        //button to share the files to lightRoom
        btnShare = findViewById(R.id.btnShare);
        //progress panel elements
        rlProgressPanel = findViewById(R.id.rlProgressPanel);
        tvCopiedFilesCount = findViewById(R.id.tv_copied_files_count);
        tvPercentage = findViewById(R.id.tv_percentage);
        tvCopying = findViewById(R.id.tv_copying);
        tvCompleted = findViewById(R.id.tv_completed);
        tvSyncDuration = findViewById(R.id.tv_sync_duration);
        tvSyncSpeed = findViewById(R.id.tv_sync_speed);
        progressBar = findViewById(R.id.probar);
    }
    private void clickListeners(){
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareToLightRoom();
            }
        });
        btnManualSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStoragePermissionGranted()){
                    if(SyncFileUtility.isSourceReadable()){
                        if(!App.isMyServiceRunning(MyService.class,ActivityController.this)){
                            //set preferences field to empty before sync start
                            setPreferenceFieldEmpty();
                           //setting up Progress panel before syn start
                            tvCopying.setVisibility(View.VISIBLE);
                            progressBar.setProgress(0);
                            tvCopiedFilesCount.setText("");
                            tvPercentage.setText("0%");
                            tvCompleted.setVisibility(View.INVISIBLE);
                            rlProgressPanel.setVisibility(View.VISIBLE);
                            btnManualSync.setEnabled(false);
                            startService(new Intent(ActivityController.this,SyncService.class));
                            //serviceIntent = new Intent(ActivityController.this, SyncService.class);
                            //ContextCompat.startForegroundService(ActivityController.this, serviceIntent);
                        }
                        else {
                            App.showToast("Sync is already in progress");
                        }
                    }
                    else {
                        App.showToast("USB not found");
                    }
                }
            }
        });
        btnAutoSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStoragePermissionGranted()){
                    if(App.isMyServiceRunning(MyService.class,ActivityController.this)){
                        serviceIntent = new Intent(ActivityController.this, MyService.class);
                        stopService(serviceIntent);
                        setBtnAutoSyncOFFGui();
                    }else {
                        String input = "File Sync is Activated";
                        serviceIntent = new Intent(ActivityController.this, MyService.class);
                        serviceIntent.putExtra("inputExtra", input);
                        ContextCompat.startForegroundService(ActivityController.this, serviceIntent);
                        setBtnAutoSyncOnGui();
                    }
                }
            }
        });
        btnChooseFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStoragePermissionGranted()){
                    final Intent chooserIntent = new Intent(ActivityController.this, DirectoryChooserActivity.class);
                    final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                            .newDirectoryName("DirChooserSample")
                            .allowReadOnlyDirectory(true)
                            .allowNewDirectoryNameModification(true)
                            .build();
                    // REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
                    chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
                    startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
                }
            }
        });
        localBroadCast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(LocalBroadcast.ACTION_SYNCED_RESULT)){
                    tvCopying.setVisibility(View.INVISIBLE);
                    btnManualSync.setEnabled(true);
                    loadSharedPreferenceData();
                    String fileSynced = SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES,"0");
                    if(!fileSynced.equals("0")){
                        shareToLightRoom();
                    }
                }
                if(intent.getAction().equals(LocalBroadcast.ACTION_MESSAGE_UPDATE)){
                    rlProgressPanel.setVisibility(View.VISIBLE);
                    setPreferenceFieldEmpty();
                    int copiedFiles = intent.getIntExtra(LocalBroadcast.INTENT_EXTRA_COPIED_FILES,0);
                    int totalFiles = intent.getIntExtra(LocalBroadcast.INTENT_EXTRA_TOTAL_FILES,0);
                    Log.d(TAG,copiedFiles+"/"+totalFiles);
                    if(copiedFiles==totalFiles){
                        progressBar.setMax(10);progressBar.setProgress(10);
                        tvCopiedFilesCount.setText(copiedFiles+"/"+totalFiles);
                        tvPercentage.setText(100+"%");
                        tvCompleted.setVisibility(View.VISIBLE);
                    }
                    else {
                        int percentage = (int)((copiedFiles * 100.0f) / totalFiles);
                        progressBar.setMax(totalFiles);
                        progressBar.setProgress(copiedFiles);
                        tvCopiedFilesCount.setText(copiedFiles+"/"+totalFiles);
                        tvPercentage.setText(percentage+"%");
                        if(totalFiles==copiedFiles){
                            tvCompleted.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcast.ACTION_SYNCED_RESULT);
        intentFilter.addAction(LocalBroadcast.ACTION_MESSAGE_UPDATE);
        registerReceiver(localBroadCast,intentFilter);
    }

    private void setPreferenceFieldEmpty() {
        tvLastSyncTime.setText("");
        tvNoOFilesSynced.setText("");
        tvSyncSpeed.setText("");
        tvSyncedDataAmount.setText("");
        tvSyncDuration.setText("");
        tvLastSyncStatus.setText("");
        tvNoOFilesSkipped.setText("");
        tvReadyToShare.setText("");
        btnShare.setVisibility(View.GONE);
    }

    private void setInitialGUi(){
        if(App.isMyServiceRunning(MyService.class,this)){
            setBtnAutoSyncOnGui();
        }else {
            setBtnAutoSyncOFFGui();
        }
        if(App.isMyServiceRunning(SyncService.class,this)){
            rlProgressPanel.setVisibility(View.VISIBLE);
        }

    }
    private void loadSharedPreferenceData(){
        // Last Sync data
        tvLastSyncTime.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_TIME,"7th August,2020 11:25:28"));
        tvSyncedDataAmount.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_DATA_AMOUNT,"5.7 GB"));
        tvLastSyncStatus.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_STATUS,"Completed"));
        tvNoOFilesSynced.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES,"10"));
        tvNoOFilesSkipped.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_SKIPPED_FILES,"0"));
        tvSyncDuration.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_DURATION,"8 second"));
        tvSyncSpeed.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_SPEED,"36 Mb/s"));
        int filesReadyToShare = getFilesReadyToShare().size();
        tvReadyToShare.setText(filesReadyToShare+"");
        if(filesReadyToShare<=0)
            btnShare.setVisibility(View.GONE);
        else
            btnShare.setVisibility(View.VISIBLE);
        //Destination Folder
        tvFolderPath.setText(SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,SharedPref.DEFAULT_DESTINATION_FOLDER));
    }
    private void setBtnAutoSyncOnGui(){
        btnAutoSync.setBackground(getDrawable(R.drawable.rec_shape2));
    }
    private void setBtnAutoSyncOFFGui(){
        btnAutoSync.setBackground(getDrawable(R.drawable.rec_shape));
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                SharedPref.write(SharedPref.KEY_DESTINATION_FOLDER,data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
               tvFolderPath.setText(data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
            }
        }
    }

    private ArrayList<String> getFilesReadyToShare(){
        String filePaths = SharedPref.read(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"");
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

    private void shareToLightRoom(){
        File destinationFolder = new File(SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,SharedPref.DEFAULT_DESTINATION_FOLDER));
        Log.d("abc",destinationFolder.getAbsolutePath());
        if(destinationFolder.exists()){
            // File destinationFolder = new File(tvFolderPath.getText().toString());
            Log.d("abc",destinationFolder.getAbsolutePath());
            ArrayList<String> syncedFiles = getFilesReadyToShare();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putExtra(Intent.EXTRA_SUBJECT, "CR3 files.");
            intent.setType("image/jpeg"); /* This example is sharing jpeg images. */
            intent.setPackage("com.adobe.lrmobile");

            ArrayList<Uri> files = new ArrayList<Uri>();

            for(String path : syncedFiles /* List of the files you want to send */) {
              File file = new File(path);
                if(file.exists()){
                    Uri uri = FileProvider.getUriForFile(this, "com.example.filesynchor.provider",file);
                    files.add(uri);
                }

            }
            Log.d("abc","no of files "+files.size()+"");

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.adobe.lrmobile");
            //startActivity( launchIntent );

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            // SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"");
           // startActivity(intent);
            startActivities(new Intent[]{launchIntent,intent});

            //startActivity(Intent.createChooser(intent, "Share the Pictures"));
        }
        else {
            Toast.makeText(this,"Destination Folder doesn't exist",Toast.LENGTH_LONG).show();
        }




    }

    @Override
    protected void onResume() {
        super.onResume();
        setInitialGUi();
        loadSharedPreferenceData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localBroadCast);
    }


    public void onClick(View view) {
        bottomSheetDialog.dismiss();
    }
}
