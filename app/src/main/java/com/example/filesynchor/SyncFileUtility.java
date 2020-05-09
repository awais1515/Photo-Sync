package com.example.filesynchor;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.filesynchor.App.TAG;

public class SyncFileUtility {

    private static final String dstFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/AutoSync Eliaz/";
    private static final String rootFolder ="/storage/";
    private static String sdCardPath;


    public  static void  syncFolder(){
        Log.d(TAG,"SyncFolder is called");
        File directory = new File(dstFolder);
        if(!directory.exists()){
            Log.d(TAG,"Directory Created");
            directory.mkdir();
        }else{
            Log.d(TAG,"Directory Already Present");
        }

        SharedPref.write(SharedPref.KEY_LAST_SYNC_NO_OF_FILES,0);
        SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"");
        //before sync start
        String sdCardPath = getSdCardStoragePath();
        if(isSourceReadable()){
            copyFileOrDirectory(sdCardPath,dstFolder);
            //after sync
            String timeStamp = new SimpleDateFormat("MMMM dd,yyyy HH:mm:ss").format(new Date());
            Log.d(TAG,"Time Started Sync: "+timeStamp);
            SharedPref.write(SharedPref.KEY_LAST_SYNC_TIME,timeStamp);
            if(SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES,0)==0)
                SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"All the files are already upto date");
        }




    }
    private static void copyFileOrDirectory(String srcDir, String dstDir) {
        try {
            File src = new File(srcDir);
            File dst = new File(dstDir,src.getName());

            if (src.isDirectory()) {
              //  Log.d(TAG,"Source is Directory  "+src.getAbsolutePath());
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();

                    copyFileOrDirectory(src1, dst1);
                }
            } else {
               // Log.d(TAG,"Source is a File "+src.getAbsolutePath());
                //if(src.getAbsolutePath().endsWith(".jpg")||src.getAbsolutePath().endsWith(".png"))
                if(src.getAbsolutePath().endsWith(".CR3"))
                copyFile(src, dst);
            }
        } catch (Exception e) {
            Log.d(TAG,"Source isn't a file or Directory...May be not available" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void copyFile(File sourceFile, File destFile) throws IOException {
        Log.d(TAG,"copy method called    source path: "+sourceFile.getAbsolutePath()+"  destination path: "+destFile.getAbsolutePath());
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();


        if (!destFile.exists()||destFile.length()<sourceFile.length()) {
            destFile.createNewFile();
            FileChannel source = null;
            FileChannel destination = null;
            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());

                /*final long blockSize = 1024;
                long position = 0;
                while (destination.transferFrom(source, position, blockSize) > 0) {
                    position += blockSize;
                }*/



                Log.d("abcd", "Copied: " + destFile.getAbsolutePath());
                SharedPref.write(SharedPref.KEY_LAST_SYNC_NO_OF_FILES, SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES, 0) + 1);
                String fileNo = SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES, 0) + ") ";
                SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS, SharedPref.read(SharedPref.KEY_LAST_SYNC_FILE_PATHS, "") + fileNo + "  " + destFile.getName() + "\n");
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        }

    }
    public static boolean isSourceReadable(){
        sdCardPath = getSdCardStoragePath();
        if(sdCardPath!=null){
            File file = new File(sdCardPath);
            if(file.exists())
                if (file.isDirectory())
                    if(file.list()!=null){
                        return true;
                    }
        }

        return false;
    }
    public static String getSdCardStoragePath(){
        File f = new File(rootFolder);

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
        return files;

    }
}
