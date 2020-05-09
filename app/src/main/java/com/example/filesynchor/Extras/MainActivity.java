package com.example.filesynchor.Extras;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filesynchor.R;
import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    Button btnNotification;
    TextView tvDeviceInfo,tvDirectories;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnNotification = findViewById(R.id.btn_notification);
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo);
        tvDirectories = findViewById(R.id.tvDirectories);

        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNotification();
                Toast.makeText(MainActivity.this,"Buttnon clicked",Toast.LENGTH_LONG).show();
            }
        });

        loadDevicesInfo();
        listUSBDeices();


    }

    private void addNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this,"AWAIS")
                .setSmallIcon(R.drawable.ic_android)
                .setContentTitle("title")
                .setContentText("content")

                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("AWAIS", "AHMAD", NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(0, mBuilder.build());
    }

    private void loadDevicesInfo(){
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            String info = device.getDeviceName()+"      "+device.getManufacturerName()+"      "+device.getProductName()+"      "+device.getSerialNumber();
            tvDeviceInfo.setText(tvDeviceInfo.getText()+info+"\n");
            UsbDeviceConnection connection = manager.openDevice(device);

            //your code
        }
    }
    private void listFiles(){
        try {
            Process process = Runtime.getRuntime().exec("ls /dev/bus/usb/001/003");
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String listOfFiles = "";
            String line;
            while ((line = in.readLine()) != null) {
                listOfFiles += line;
            }
            tvDirectories.setText(listOfFiles);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"IO Exception",Toast.LENGTH_LONG).show();
        }
    }

    private void listUSBDeices(){
        UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(this /* Context or Activity */);
        String TAG = "abcd";
        for(UsbMassStorageDevice device: devices) {
            // before interacting with a device you need to call init()!

            try {
                device.init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String line ="";
            // Only uses the first partition on the device
            FileSystem currentFs = device.getPartitions().get(0).getFileSystem();
            line=line+ " Capacity: " + currentFs.getCapacity()+"\n";
            line=line+"Occupied Space: " + currentFs.getOccupiedSpace()+"\n";
            line=line+"Free Space: " + currentFs.getFreeSpace()+"\n";
            line=line+"Chunk size: " + currentFs.getChunkSize()+"\n";
            tvDirectories.setText(line+"\n\n\n");
        }
    }
   /* PendingIntent permissionIntent;
    //UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                        }
                    }
                    else {
                        Log.d("abcd", "permission denied for device " + device);
                    }
                }
            }
        }
    };*/

}
