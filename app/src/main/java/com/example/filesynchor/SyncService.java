package com.example.filesynchor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import static com.example.filesynchor.App.TAG;

public class SyncService extends Service implements  SyncProgress{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Service is started");
        String input = intent.getStringExtra("inputExtra");
        startSyncThread();
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"Service is Destroyed");
    }
    private void startSyncThread(){
       new Thread(new Runnable() {
           @Override
           public void run() {
                try {
                    Log.d(TAG,"FileSynchor thread name: "+Thread.currentThread().getName());
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    int i;
                    for(i=1;i<=9;i++){

                        if(SyncFileUtility.isSourceReadable()){
                            Log.d(TAG,"USB is ready");
                            //Notifications.addNotification(getApplicationContext(),"Syncing your files...","");
                            SyncFileUtility.syncFolder(SyncService.this);
                            //shareToLightRoom();
                           // Notifications.addNotification(context.getApplicationContext(),"Synced files Successfully","");
                            Log.d("abcd","synced");
                            sendLocalBroadcast();
                            break;
                        }
                        else {
                            Thread.sleep(7000);
                            Log.d(TAG,"USB not ready yet");
                        }
                    }
                    if(i>9){
                        //Notifications.addNotification(context.getApplicationContext(),"Sync Failed","Couldn't connect to USB");
                        Log.d("abcd","sync failed");
                    }

                } catch (InterruptedException e) {
                    //registerReceiver(broadcastReceiver,filter);
                    e.printStackTrace();
                    Log.d("abcd","Error in thread");
                }
                finally {
                    SyncService.this.stopSelf();
                }
            }
        }).start();
   }

    @Override
    public void onProgressUpdate(int copiedFiles, int totalFiles) {
        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        Intent intent = new Intent();
        intent.setAction(LocalBroadcast.ACTION_MESSAGE_UPDATE);
        intent.putExtra(LocalBroadcast.INTENT_EXTRA_COPIED_FILES,copiedFiles);
        intent.putExtra(LocalBroadcast.INTENT_EXTRA_TOTAL_FILES,totalFiles);
        sendBroadcast(intent);
    }
    private void sendLocalBroadcast(){
        Intent intent = new Intent();
        intent.setAction(LocalBroadcast.ACTION_SYNCED_RESULT);
        sendBroadcast(intent);
    }
    /*private ArrayList<String> getFilesReadyToShare(){
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
        if(destinationFolder.exists()){
            // File destinationFolder = new File(tvFolderPath.getText().toString());
            Log.d("abc",destinationFolder.getAbsolutePath());
            ArrayList<String> syncedFiles = getFilesReadyToShare();
            if(syncedFiles.size()>0){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.putExtra(Intent.EXTRA_SUBJECT, "CR3 files.");
                intent.setType("image/jpeg"); *//* This example is sharing jpeg images. *//*
                intent.setPackage("com.adobe.lrmobile");

                ArrayList<Uri> files = new ArrayList<Uri>();

                for(String path : syncedFiles *//* List of the files you want to send *//*) {
                    File file = new File(path);
                    if(file.exists()){
                        Uri uri = FileProvider.getUriForFile(this, "com.example.filesynchor.provider",file);
                        files.add(uri);
                    }

                }
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.adobe.lrmobile");
                //startActivity(launchIntent);

                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                // SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"");
                //startActivity(intent);
                startActivities(new Intent[]{launchIntent,intent});

                //startActivity(Intent.createChooser(intent, "Share the Pictures"));
            }

        }




    }*/
}
