package com.example.filesynchor;

import android.util.Log;

import com.example.filesynchor.Database.DataRepo;
import com.example.filesynchor.FileManager.FileUtil;
import com.example.filesynchor.FileManager.StorageDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.filesynchor.App.TAG;

public class SyncFileUtility {

    //private static final String dstFolder = SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,SharedPref.DEFAULT_DESTINATION_FOLDER);
    //private static final String rootFolder ="/storage/";
    private static String usbStoragPath;
    private static SyncProgress syncProgress;
    private static ArrayList<String> fileNames = new ArrayList<>();
    private static int totalFiles,copiedFiles,skippedFiles = 0;
    private static long copiedBytes = 0;

    public  static void  syncFolder(SyncProgress syncProgress){
        SyncFileUtility.syncProgress = syncProgress;
        Log.d(TAG,"SyncFolder is called");
        File directory = new File(getDestinationFolder());
        if(!directory.exists()){
            Log.d(TAG,"Directory Created");
            directory.mkdir();
        }else{
            Log.d(TAG,"Directory Already Present");
        }


        String usbStoragePath = getUSBStoragePath();
        if(isSourceReadable()){
            //before sync start
            totalFiles = 0;copiedFiles = 0;copiedBytes=0;skippedFiles=0;
            Date startTime = new Date();
            fileNames.clear();
            countFiles(usbStoragePath);
            App.showLog(totalFiles+"");
            if(totalFiles==0){
                syncProgress.onProgressUpdate(copiedFiles,totalFiles);
            }else {
                SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"");
                copyFileOrDirectory(usbStoragePath);
            }
            //after sync
            saveResult(startTime,copiedFiles,totalFiles,copiedBytes);
        }




    }
    private static void copyFileOrDirectory(String srcDir) {
        try {
            File src = new File(srcDir);


            if (src.isDirectory()) {
              //  Log.d(TAG,"Source is Directory  "+src.getAbsolutePath());
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    copyFileOrDirectory(src1);
                }
            } else {
               // Log.d(TAG,"Source is a File "+src.getAbsolutePath());
                //if(src.getAbsolutePath().endsWith(".jpg")||src.getAbsolutePath().endsWith(".png")||src.getAbsolutePath().endsWith(".CR3"))
                if(src.getAbsolutePath().endsWith(".CR3"))
                    if(!src.isHidden())
                        copyFile(src);
            }
        } catch (Exception e) {
            Log.d(TAG,"Source isn't a file or Directory...May be not available" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void copyFile(File sourceFile) throws IOException {

        /*if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();*/
        File destFile = new File(getDestinationFolder(),sourceFile.getName());
        Log.d(TAG,"copy method called    source path: "+sourceFile.getAbsolutePath()+"  destination path: "+destFile.getAbsolutePath());

        if (!destFile.exists()||destFile.length()<sourceFile.length()) {

            boolean isCopied = FileUtil.copyFile(sourceFile,destFile);
            if(isCopied){
                copiedFiles++;
                copiedBytes+=destFile.length();
                syncProgress.onProgressUpdate(copiedFiles,totalFiles);
                SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS, SharedPref.read(SharedPref.KEY_LAST_SYNC_FILE_PATHS, "") + destFile.getAbsolutePath() + "\n");
            }
           /* destFile.createNewFile();
            FileChannel source = null;
            FileChannel destination = null;
            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
                Log.d("abcd", "Copied: " + destFile.getAbsolutePath());
                copiedFiles++;
                copiedBytes+=destFile.length();
                syncProgress.onProgressUpdate(copiedFiles,totalFiles);
                *//*final long blockSize = 2048;
                long position = 0;
                while (destination.transferFrom(source, position, blockSize) > 0) {
                    destination.transferFrom(source,position,blockSize);
                    position += blockSize;
                }*//*

                //SharedPref.write(SharedPref.KEY_LAST_SYNC_NO_OF_FILES, SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES, 0) + 1);
                //String fileNo = SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES, 0) + ") ";
                SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS, SharedPref.read(SharedPref.KEY_LAST_SYNC_FILE_PATHS, "") + destFile.getAbsolutePath() + "\n");
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }*/
        }

    }
    public static boolean isSourceReadable(){
        usbStoragPath = getUSBStoragePath();
        if(usbStoragPath !=null){
            File file = new File(usbStoragPath);
            if(file.exists())
                if (file.isDirectory())
                    if(file.list()!=null){
                        return true;
                    }
        }

        return false;
    }
    public static String getUSBStoragePath(){
      /* File f = new File(rootFolder);

        // Exists or not
        String files="SD Card Content: "+f.getAbsolutePath()+"\n\n\n";
        if (f.exists()){
            for(String path: f.list()){
                Log.d(TAG,"file is a directory"+path);
                if(path.length()==9){
                    Log.d(TAG,"Directory name length is 9");
                    if(path.charAt(4)=='-')
                    {
                        return rootFolder+path+"/";
                    }
                }
            }
            Log.d(TAG,"Couldn't find any SD CARD");
        }
        else{
            Log.d(TAG,"Couldn't find Storage");
        }
        return files;*/
       return StorageDirectory.getUSBStoragePath()+"/";

    }
    private static void saveResult(Date startTime,int copiedFiles,int totalFiles,long copiedBytes){
        Data data = new Data();
        data.setPaths(SharedPref.read(SharedPref.KEY_LAST_SYNC_FILE_PATHS, ""));
        double syncDuration =getTimeDifference(startTime,new Date());
        String timeStamp = new SimpleDateFormat("dd.MMMM  HH:mm:ss").format(new Date());
        Log.d(TAG,"Time Started Sync: "+timeStamp);
        SharedPref.write(SharedPref.KEY_LAST_SYNC_TIME,timeStamp);
        data.setSyncTime(timeStamp);
        SharedPref.write(SharedPref.KEY_LAST_SYNC_NO_OF_FILES,copiedFiles+"");
        data.setSyncedFiles(copiedFiles);
        SharedPref.write(SharedPref.KEY_LAST_SYNC_DATA_AMOUNT,String.format("%.2f",(float)copiedBytes/1024/1024/1024) +" GB");
        data.setSyncedGB(String.format("%.2f",(float)copiedBytes/1024/1024/1024) +" GB");
        SharedPref.write(SharedPref.KEY_LAST_SYNC_SKIPPED_FILES,skippedFiles+"");
        if(totalFiles==0){
            SharedPref.write(SharedPref.KEY_LAST_SYNC_SPEED,0+" Mb/s");
            SharedPref.write(SharedPref.KEY_LAST_SYNC_DURATION,0+" seconds");
        }
        else {
            if(syncDuration<=0)
                SharedPref.write(SharedPref.KEY_LAST_SYNC_SPEED,copiedBytes/1024/1024+" Mb/s");
            else{
                long speed = copiedBytes/1024/1024/(int)syncDuration;
                SharedPref.write(SharedPref.KEY_LAST_SYNC_SPEED,speed +" Mb/s");
            }

            SharedPref.write(SharedPref.KEY_LAST_SYNC_DURATION,getDuration(syncDuration));
            App.showLog(syncDuration+"");
            Log.d(TAG,getDuration(syncDuration));
        }

        if(totalFiles==copiedFiles)
        {
            SharedPref.write(SharedPref.KEY_LAST_SYNC_STATUS,"Completed");
            data.setStatus("Completed");
        }
        else
        {
            SharedPref.write(SharedPref.KEY_LAST_SYNC_STATUS,"Incomplete");
            data.setStatus("Incomplete");
        }
        DataRepo.insertData(data);

    }
    private static void countFiles(String srcDir) {
        try {
            File src = new File(srcDir);
            if (src.isDirectory()) {
                //  Log.d(TAG,"Source is Directory  "+src.getAbsolutePath());
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    countFiles(src1);
                }
            } else {
                // Log.d(TAG,"Source is a File "+src.getAbsolutePath());
                //if(src.getAbsolutePath().endsWith(".jpg")||src.getAbsolutePath().endsWith(".png")||src.getAbsolutePath().endsWith(".CR3"))
                if(src.getAbsolutePath().endsWith(".CR3"))
                    if(!src.isHidden())
                    {
                        File dst = new File(getDestinationFolder(),src.getName());
                        if (!dst.exists()||dst.length()<src.length()){
                            if(!fileNames.contains(src.getName()))
                            {
                                totalFiles++;
                                fileNames.add(src.getName());
                            }
                        }
                        else
                        {
                            skippedFiles++;
                        }

                    }

            }
        } catch (Exception e) {
            Log.d(TAG,"Source isn't a file or Directory...May be not available " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static double getTimeDifference(Date startTime,Date endTime){
        long difference =  endTime.getTime()-startTime.getTime();
        App.showLog(difference+"");
        return difference/1000;
    }
    private static String getDuration(double duration){
        if(duration>=60){
            int min = (int)duration/60;
            int sec = (int)duration%60;
            return min+" minute "+sec+" second";
        }
        else {
            if(duration<0){
                return String.format("%.1f", duration +"second");
            }
            else {
                return ((int) duration)+" second";
            }
        }
    }

    private static String getDestinationFolder(){
        String dstFolder = SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,SharedPref.DEFAULT_DESTINATION_FOLDER);
        return dstFolder;
    }



}
