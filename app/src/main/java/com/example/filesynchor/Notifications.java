package com.example.filesynchor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Notifications {
    public static void addNotification(Context context, String title, String content) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,"AWAIS")
                .setSmallIcon(R.drawable.ic_android)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentText(content)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("AWAIS", "AHMAD", NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(0, mBuilder.build());
    }
}
