package com.example.filesynchor;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

public class App extends Application {
    public static final String TAG = "abcd";
    public static final String CHANNEL_ID = "exampleServiceChannel";
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        App.context = this;
        SharedPref.init(this);
        createNotificationChannel();
        /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.enableVibration(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    public static Context getAppContext(){
        return App.context;
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public static void showToast(String message){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }
    public static void showLog(String message){
        Log.d(TAG,message);
    }

}
