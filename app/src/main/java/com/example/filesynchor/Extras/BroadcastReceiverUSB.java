package com.example.filesynchor.Extras;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.filesynchor.App;
import com.example.filesynchor.MyService;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.filesynchor.App.TAG;

public class BroadcastReceiverUSB extends BroadcastReceiver {
    String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";
    String USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    String USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
         Log.d("abcd", "Received Broadcast: " + action);
        //Toast.makeText(context.getApplicationContext(),action,Toast.LENGTH_LONG).show();
        //Check if change in USB state
           if (action.equals(USB_ATTACHED)) {
               if(!App.isMyServiceRunning(MyService.class,context)){
                   /*Intent serviceIntent = new Intent(context, MyService.class);
                   context.startService(serviceIntent);*/
                   String input = "File Sync is Activated";
                   Intent serviceIntent = new Intent(context.getApplicationContext(), MyService.class);
                   serviceIntent.putExtra("inputExtra", input);
                   ContextCompat.startForegroundService(context.getApplicationContext(), serviceIntent);
                   Log.d(TAG,"Starting Sync Service");
               }
               else {
                   Log.d(TAG,"Service is already running");
               }
            } else if(action.equals(USB_DETACHED)){
                // USB was disconnected
                Log.d("abcd","Usb Disconnected");
            }

    }




}


