package com.example.filesynchor.Extras;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.filesynchor.R;
import com.example.filesynchor.SyncFileUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Splash extends AppCompatActivity {

    String TAG = "abcd";
    TextView tvDeviceInfo;

    private static final String dstFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/FileSync";
    //private static final String dstFolder = Environment.getRootDirectory().getAbsolutePath()+"/FileSync";
    private static final String srcFolder = "/storage/191D-0C26/";
    private Button btnSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        btnSync = findViewById(R.id.btnSync);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // SyncFileUtility.syncFolder();

            }
        });

       /* UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
        Collection<UsbDevice> usbDeviceList = usbManager.getDeviceList().values();
        if(usbManager.getDeviceList().values().iterator().hasNext()){
           // usbManager.requestPermission(usbManager.getDeviceList().values().iterator().next(), permissionIntent);
        }*/
       // tvDeviceInfo.setText(ExternalStorage.getSdCardPath());
        /*Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
        File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
        File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);*/


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
            loadDirectories();
        }
        else {
            isStoragePermissionGranted();
        }
    }

    private void loadDirectories(){
        File f = new File("/storage/");
        //File f = new File("/storage/self");
        f.getAbsolutePath();
        //File f = new File("/storage/256f-1435/");
        // Check if the specified file
        // Exists or not
        String files="SD Card Content: "+f.getAbsolutePath()+"\n\n\n";
        if (f.exists()){
            for(String path: f.list()){
                files+=path+"\n";
            }
            tvDeviceInfo.setText(files);
        }
        else
            tvDeviceInfo.setText("Path not found");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isStoragePermissionGranted()){
            loadDirectories();
        }
    }

}
