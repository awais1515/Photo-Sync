package com.example.filesynchor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import static com.example.filesynchor.App.CHANNEL_ID;
import static com.example.filesynchor.App.TAG;

public class MyService extends Service {
    BroadcastReceiver broadcastReceiver;
    private static boolean LOCK = false;
    @Override
    public void onCreate() {
        super.onCreate();
        final IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                Log.d("abcd","USB CONNECTED");
               // unregisterReceiver(broadcastReceiver);
                //SyncFileUtility.syncFolder();

                if(!LOCK){
                   LOCK =true;


                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.d(TAG,"FileSynchor thread name: "+Thread.currentThread().getName());
                                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                                int i;
                                for(i=1;i<=9;i++){
                                    Thread.sleep(7000);
                                    if(SyncFileUtility.isSourceReadable()){
                                        Log.d(TAG,"USB is ready");
                                        Notifications.addNotification(context.getApplicationContext(),"Syncing your files...","");
                                        SyncFileUtility.syncFolder();
                                        Notifications.addNotification(context.getApplicationContext(),"Synced files Successfully","");
                                        Log.d("abcd","synced");
                                        break;
                                    }
                                    else {
                                        Log.d(TAG,"USB not ready yet");
                                    }
                                }
                                if(i>9){
                                    Notifications.addNotification(context.getApplicationContext(),"Sync Failed","Couldn't connect to USB");
                                    Log.d("abcd","sync failed");
                                }
                                LOCK = false;

                                //registerReceiver(broadcastReceiver,filter);



                            } catch (InterruptedException e) {
                                LOCK =false;
                                //registerReceiver(broadcastReceiver,filter);
                                e.printStackTrace();
                                Log.d("abcd","Error in thread");
                            }
                        }
                    }).start();
                }
            }
        };

      registerReceiver(broadcastReceiver,filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Service thread name: "+Thread.currentThread().getName());
        String input = intent.getStringExtra("inputExtra");

        Intent notificationIntent = new Intent(this, ActivityFileSyncController.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AutoSync File Activated")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread
        //stopSelf();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        Log.d(TAG,"Service is destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendLocalBroadcast(){
        Intent intent = new Intent();
        intent.setAction(LocalBroadcast.ACTION_SYNCED_RESULT);
        sendBroadcast(intent);

    }


}
