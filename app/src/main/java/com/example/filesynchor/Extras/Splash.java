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


            }
        });


    }



}
