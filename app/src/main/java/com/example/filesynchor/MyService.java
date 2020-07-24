package com.example.filesynchor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import static com.example.filesynchor.App.CHANNEL_ID;
import static com.example.filesynchor.App.TAG;

public class MyService extends Service implements SyncProgress{
    private static final int CHANNEL_2_ID = 2;
    BroadcastReceiver broadcastReceiver;
    NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;
    private static boolean LOCK = false;
    private Intent notificationIntent;
    private PendingIntent pendingIntent;
    //action intents
    private Intent syncNowIntent;
    private PendingIntent syncNowPendingIntent;
    private Vibrator vibrator;

    private static final long VIBRATE_DURATION = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        //to perform syn action
        syncNowIntent = new Intent(this, MyService.class);
        syncNowIntent.setAction(LocalBroadcast.ACTION_SYNC_NOW);
        syncNowPendingIntent = PendingIntent.getService(this, 0, syncNowIntent, 0);
        //to start activity
        notificationIntent = new Intent(this, ActivityController.class);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notificationManager = NotificationManagerCompat.from(this);
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_android)
                .setContentTitle("PhotoSync")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);


        final IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                Log.d("abcd","USB CONNECTED");
                //Notifications.addNotification(context.getApplicationContext(),"USB Connected","");
                notificationBuilder.setContentTitle("USB Connected");
                notificationBuilder.setContentText("");
                notificationBuilder.setProgress(0,0,false);
                notificationManager.notify(CHANNEL_2_ID,notificationBuilder.build());
                vibrator.vibrate(VIBRATE_DURATION);
               // unregisterReceiver(broadcastReceiver);
                //SyncFileUtility.syncFolder();

                if(!LOCK){
                   LOCK =true;
                   startSyncThread();
                }
            }
        };
        registerReceiver(broadcastReceiver,filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction()!=null){
            if(intent.getAction().equals(LocalBroadcast.ACTION_SYNC_NOW)){
                App.showLog("You should syn now");
                if(!LOCK){
                    vibrator.vibrate(VIBRATE_DURATION);
                    LOCK =true;
                    notificationBuilder.setContentTitle("Sync is getting ready... Please Wait");
                    notificationBuilder.setContentText("");
                    notificationBuilder.setProgress(0,0,false);
                    notificationManager.notify(CHANNEL_2_ID,notificationBuilder.build());
                    startSyncThread();
                }
            }
        }
        else {
            vibrator.vibrate(VIBRATE_DURATION);
            Log.d(TAG,"Service thread name: "+Thread.currentThread().getName());
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("AutoSync File Activated")
                    .setSmallIcon(R.drawable.appicon)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .addAction(R.drawable.appicon,"Tap to Sync", syncNowPendingIntent)
                    .build();

            startForeground(1, notification);
        }


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

    @Override
    public void onProgressUpdate(int copiedFiles, int totalFiles) {
        App.showLog("Copying "+copiedFiles+"/"+totalFiles);
        if(totalFiles==0){
            notificationBuilder.setContentTitle("Synced Successfully");
            notificationBuilder.setContentText("All the files are already synced");
        }
        else {
            notificationBuilder.setContentTitle("Syncing");
            notificationBuilder.setVibrate(new long[] { 0, 0, 0, 0,0 });
            notificationBuilder.setContentText("Copied "+copiedFiles+"/"+totalFiles);
            notificationBuilder.setProgress(totalFiles,copiedFiles,false);
        }
        notificationManager.notify(CHANNEL_2_ID,notificationBuilder.build());
        Intent intent = new Intent();
        intent.setAction(LocalBroadcast.ACTION_MESSAGE_UPDATE);
        intent.putExtra(LocalBroadcast.INTENT_EXTRA_COPIED_FILES,copiedFiles);
        intent.putExtra(LocalBroadcast.INTENT_EXTRA_TOTAL_FILES,totalFiles);
        sendBroadcast(intent);
    }
    // main working thread
    private void startSyncThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                    Log.d(TAG,"FileSynchor thread name: "+Thread.currentThread().getName());
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    int i;
                    for(i=1;i<=9;i++){
                        if(SyncFileUtility.isSourceReadable()){
                            Log.d(TAG,"USB is ready");
                            // Notifications.addNotification(context.getApplicationContext(),"Syncing your files...","");
                            SyncFileUtility.syncFolder(MyService.this);
                            notificationBuilder.setContentTitle("Synced Successfully");
                            notificationBuilder.setOngoing(false);
                            notificationBuilder.setAutoCancel(true);
                            notificationManager.notify(CHANNEL_2_ID,notificationBuilder.build());
                            vibrator.vibrate(VIBRATE_DURATION);
                            //Notifications.addNotification(context.getApplicationContext(),"Synced files Successfully","");
                            sendLocalBroadcast();
                            Log.d("abcd","synced");
                            break;
                        }
                        else {
                            Log.d(TAG,"USB not ready yet");
                        }
                    }
                    if(i>9){
                        notificationBuilder.setAutoCancel(true);
                        notificationBuilder.setContentTitle("Sync Failed");notificationBuilder.setContentText("USB Disconnected");
                        notificationManager.notify(CHANNEL_2_ID,notificationBuilder.build());
                        vibrator.vibrate(VIBRATE_DURATION);

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
