package com.example.filesynchor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.filesynchor.App.TAG;

public class ActivityFileSyncController extends AppCompatActivity {

    Button btnStartService,btnStopService,btnSync;
    ImageView icon;
    TextView tvServiceStatus,tvLastSyncTime,tvNoOFilesSynced,tvSyncedFilePaths;
    Intent serviceIntent;
    BroadcastReceiver localBroadCast;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_service);
        Log.d(TAG,"Activity thread name: "+Thread.currentThread().getName());
        btnStartService = findViewById(R.id.btnStartService);
        btnSync = findViewById(R.id.btnSync);
        btnStopService = findViewById(R.id.btnStopService);
        tvServiceStatus = findViewById(R.id.tv_status);
        tvNoOFilesSynced = findViewById(R.id.tvNoOfFilesSynced);
        tvLastSyncTime = findViewById(R.id.tvLastSyncTime);
        tvSyncedFilePaths = findViewById(R.id.tvSyncedFilePaths);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Syncing files...");
        progressDialog.setCancelable(false);
        icon = findViewById(R.id.ivIcon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStoragePermissionGranted()){
                    if(isMyServiceRunning(MyService.class)){
                        serviceIntent = new Intent(ActivityFileSyncController.this, MyService.class);
                        stopService(serviceIntent);
                        offSyncGui();
                    }else {
                        String input = "File Sync is Activated";
                        serviceIntent = new Intent(ActivityFileSyncController.this, MyService.class);
                        serviceIntent.putExtra("inputExtra", input);
                        ContextCompat.startForegroundService(ActivityFileSyncController.this, serviceIntent);
                        onSyncGui();
                    }
                }

            }
        });
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // progressDialog.show();
                if(isStoragePermissionGranted()){
                    if(SyncFileUtility.isSourceReadable()){
                        progressDialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                                //SyncFileUtility.syncFolder();
                                sendLocalBroadcast();
                            }
                        }).start();
                    }
                    else {
                        Toast.makeText(ActivityFileSyncController.this, "Unable to Detect USB", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        if(isMyServiceRunning(MyService.class)){
            onSyncGui();
        }else {
            offSyncGui();
        }

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startService(new Intent(getBaseContext(), SyncService.class));
               // Toast.makeText(TestService.this,"start clicked",Toast.LENGTH_LONG).show();
                String input = "File Sync is Activated";

                serviceIntent = new Intent(ActivityFileSyncController.this, MyService.class);
                serviceIntent.putExtra("inputExtra", input);

                ContextCompat.startForegroundService(ActivityFileSyncController.this, serviceIntent);
            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stopService(new Intent(getBaseContext(), SyncService.class));
                //Toast.makeText(TestService.this,"stop clicked",Toast.LENGTH_LONG).show();
                serviceIntent = new Intent(ActivityFileSyncController.this, MyService.class);
                stopService(serviceIntent);
            }
        });
        localBroadCast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadSharedPreferenceData();
                progressDialog.dismiss();
            }
        };
        registerReceiver(localBroadCast,new IntentFilter(LocalBroadcast.ACTION_SYNCED_RESULT));
    }
    private void offSyncGui(){
        //tvServiceStatus.setText("Auto sync is off");
        icon.setBackgroundColor(getResources().getColor(R.color.colorGrey));
    }
    private void onSyncGui(){
        //tvServiceStatus.setText("Auto sync is on");
        icon.setBackgroundColor(getResources().getColor(R.color.colorGreen));
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //progressDialog.dismiss();
        loadSharedPreferenceData();
    }
    private void loadSharedPreferenceData(){
        tvLastSyncTime.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_TIME,"Not synced yet"));
        tvSyncedFilePaths.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"No Files Synced"));
        tvNoOFilesSynced.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES,"0"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localBroadCast);
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
        }
        else {
            isStoragePermissionGranted();
        }
    }
    private void sendLocalBroadcast(){
        Intent intent = new Intent();
        intent.setAction(LocalBroadcast.ACTION_SYNCED_RESULT);
        sendBroadcast(intent);
    }
}
