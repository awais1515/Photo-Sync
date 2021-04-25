package com.example.filesynchor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.example.filesynchor.FileManager.FileUtil;
import com.example.filesynchor.FileManager.StorageDirectory;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import abhishekti7.unicorn.filepicker.UnicornFilePicker;
import abhishekti7.unicorn.filepicker.utils.Constants;

import static com.example.filesynchor.App.TAG;

public class ActivityController extends AppCompatActivity {
    private static final int REQUEST_DIRECTORY = 4;
    private static final String INTERNAL_SHARED_STORAGE = "Internal shared storage";
    RelativeLayout rlProgressPanel;
    TextView
            tvLastSyncStatus,tvLastSyncTime,tvNoOFilesSynced,tvNoOFilesSkipped,
            tvSyncedDataAmount,tvFolderPath,tvCopiedFilesCount,tvPercentage,tvCopying,
            tvCompleted,tvSyncDuration,tvSyncSpeed,tvReadyToShare,tvTotalFiles;
    Button btnManualSync,btnAutoSync,btnChooseFolder,btnShare;
    ImageView btnClear;
    ProgressBar progressBar;
    Intent serviceIntent;
    BroadcastReceiver localBroadCast;
    ProgressDialog progressDialog;
    Button btnHistory;
    BottomSheet bottomSheetDialog;
    private Context mainActivity;
    private SharedPreferences sharedPrefs;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        mainActivity = this;
        init();
        clickListeners();
        showAllStorage();
       /* btnClear =(ImageView)findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              bottomSheetDialog.dismiss();
            }
        });*/

        btnHistory= findViewById(R.id.btn_SyncHistory);

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    public void showDialog() {
       bottomSheetDialog = new BottomSheet();
        bottomSheetDialog.show(getSupportFragmentManager(),"dialog");
      /* Intent intent = new Intent(this,ActivitySyncHistory.class);
       startActivity(intent);*/

    }

    private void init(){
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Syncing Data");
        //shared preferences items for last sync
        tvTotalFiles = findViewById(R.id.tvTotalFiles);
        tvLastSyncStatus = findViewById(R.id.tv_status);
        tvLastSyncTime = findViewById(R.id.tvLastSyncTime);
        tvNoOFilesSynced = findViewById(R.id.tvNoOfFilesSynced);
        tvNoOFilesSkipped = findViewById(R.id.tvNoOfSkippedFiles);
        tvSyncedDataAmount = findViewById(R.id.tvSyncedDataAmount);
        tvReadyToShare = findViewById(R.id.tvReadyToShare);
        //textView for folder path
        tvFolderPath = findViewById(R.id.tvFolder);
        //buttons for sync
        btnAutoSync = findViewById(R.id.btn_auto_sync);
        btnManualSync = findViewById(R.id.btn_manual_sync);
        //button for choose folder
        btnChooseFolder = findViewById(R.id.btn_choose_folder);
        //button to share the files to lightRoom
        btnShare = findViewById(R.id.btnShare);
        //progress panel elements
        rlProgressPanel = findViewById(R.id.rlProgressPanel);
        tvCopiedFilesCount = findViewById(R.id.tv_copied_files_count);
        tvPercentage = findViewById(R.id.tv_percentage);
        tvCopying = findViewById(R.id.tv_copying);
        tvCompleted = findViewById(R.id.tv_completed);
        tvSyncDuration = findViewById(R.id.tv_sync_duration);
        tvSyncSpeed = findViewById(R.id.tv_sync_speed);
        progressBar = findViewById(R.id.probar);
    }

    private void clickListeners(){
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareToLightRoom();
            }
        });
        btnManualSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStoragePermissionGranted()){
                    if(SyncFileUtility.isSourceReadable()){
                        if(!App.isMyServiceRunning(MyService.class,ActivityController.this)){
                            //set preferences field to empty before sync start
                            setPreferenceFieldEmpty();
                           //setting up Progress panel before syn start
                            tvCopying.setVisibility(View.VISIBLE);
                            progressBar.setProgress(0);
                            tvCopiedFilesCount.setText("");
                            tvPercentage.setText("0%");
                            tvCompleted.setVisibility(View.INVISIBLE);
                            rlProgressPanel.setVisibility(View.VISIBLE);
                            btnManualSync.setEnabled(false);
                            startService(new Intent(ActivityController.this,SyncService.class));
                            //serviceIntent = new Intent(ActivityController.this, SyncService.class);
                            //ContextCompat.startForegroundService(ActivityController.this, serviceIntent);
                        }
                        else {
                            App.showToast("Sync is already in progress");
                        }
                    }
                    else {
                        App.showToast("USB not found");
                    }
                }
            }
        });
        btnAutoSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStoragePermissionGranted()){
                    if(App.isMyServiceRunning(MyService.class,ActivityController.this)){
                        serviceIntent = new Intent(ActivityController.this, MyService.class);
                        stopService(serviceIntent);
                        setBtnAutoSyncOFFGui();
                    }else {
                        String input = "File Sync is Activated";
                        serviceIntent = new Intent(ActivityController.this, MyService.class);
                        serviceIntent.putExtra("inputExtra", input);
                        ContextCompat.startForegroundService(ActivityController.this, serviceIntent);
                        setBtnAutoSyncOnGui();
                    }
                }
            }
        });
        btnChooseFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectTargetFolder();
            }
        });
        localBroadCast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(LocalBroadcast.ACTION_SYNCED_RESULT)){
                    tvCopying.setVisibility(View.INVISIBLE);
                    btnManualSync.setEnabled(true);
                    loadSharedPreferenceData();
                    String fileSynced = SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES,"0");
                    if(!fileSynced.equals("0")){
                       shareToLightRoom();
                    }
                }
                if(intent.getAction().equals(LocalBroadcast.ACTION_MESSAGE_UPDATE)){
                    rlProgressPanel.setVisibility(View.VISIBLE);
                    setPreferenceFieldEmpty();
                    int copiedFiles = intent.getIntExtra(LocalBroadcast.INTENT_EXTRA_COPIED_FILES,0);
                    int totalFiles = intent.getIntExtra(LocalBroadcast.INTENT_EXTRA_TOTAL_FILES,0);
                    Log.d(TAG,copiedFiles+"/"+totalFiles);
                    if(copiedFiles==totalFiles){
                        progressBar.setMax(10);progressBar.setProgress(10);
                        tvCopiedFilesCount.setText(copiedFiles+"/"+totalFiles);
                        tvPercentage.setText(100+"%");
                        tvCompleted.setVisibility(View.VISIBLE);
                    }
                    else {
                        int percentage = (int)((copiedFiles * 100.0f) / totalFiles);
                        progressBar.setMax(totalFiles);
                        progressBar.setProgress(copiedFiles);
                        tvCopiedFilesCount.setText(copiedFiles+"/"+totalFiles);
                        tvPercentage.setText(percentage+"%");
                        if(totalFiles==copiedFiles){
                            tvCompleted.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocalBroadcast.ACTION_SYNCED_RESULT);
        intentFilter.addAction(LocalBroadcast.ACTION_MESSAGE_UPDATE);
        registerReceiver(localBroadCast,intentFilter);
    }

    private void setPreferenceFieldEmpty() {
        tvLastSyncTime.setText("");
        tvNoOFilesSynced.setText("");
        tvSyncSpeed.setText("");
        tvSyncedDataAmount.setText("");
        tvSyncDuration.setText("");
        tvLastSyncStatus.setText("");
        tvNoOFilesSkipped.setText("");
        tvReadyToShare.setText("");
        btnShare.setVisibility(View.GONE);
    }

    private void setInitialGUi(){
        if(App.isMyServiceRunning(MyService.class,this)){
            setBtnAutoSyncOnGui();
        }else {
            setBtnAutoSyncOFFGui();
        }
        if(App.isMyServiceRunning(SyncService.class,this)){
            rlProgressPanel.setVisibility(View.VISIBLE);
        }


    }

    private void loadSharedPreferenceData(){
        // Last Sync data
        File file = new File(SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,""));
        if(file.exists()&&file.listFiles()!=null)
            tvTotalFiles.setText(file.listFiles().length+"");
        else tvTotalFiles.setText("0");
        tvLastSyncTime.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_TIME,""));
        tvSyncedDataAmount.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_DATA_AMOUNT,""));
        tvLastSyncStatus.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_STATUS,""));
        tvNoOFilesSynced.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_NO_OF_FILES,""));
        tvNoOFilesSkipped.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_SKIPPED_FILES,""));
        tvSyncDuration.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_DURATION,""));
        tvSyncSpeed.setText(SharedPref.read(SharedPref.KEY_LAST_SYNC_SPEED,""));
        int filesReadyToShare = getFilesReadyToShare().size();
        tvReadyToShare.setText(filesReadyToShare+"");
        if(filesReadyToShare<=0)
            btnShare.setVisibility(View.GONE);
        else
            btnShare.setVisibility(View.VISIBLE);
        //Destination Folder
        tvFolderPath.setText(SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,SharedPref.DEFAULT_DESTINATION_FOLDER));
    }

    private void setBtnAutoSyncOnGui(){
        btnAutoSync.setBackground(getDrawable(R.drawable.rec_shape2));
    }

    private void setBtnAutoSyncOFFGui(){
        btnAutoSync.setBackground(getDrawable(R.drawable.rec_shape));
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DIRECTORY) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                SharedPref.write(SharedPref.KEY_DESTINATION_FOLDER,data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
               tvFolderPath.setText(data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
            }
        }
        else if (requestCode == 3) {
            Uri treeUri;
            if (resultCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = data.getData();
                // Persist URI - this is required for verification of writability.
                if (treeUri != null)
                    sharedPrefs
                            .edit()
                            .putString("URI", treeUri.toString())
                            .commit();
            } else {
                // If not confirmed SAF, or if still not writable, then revert settings.
        /* DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false, currentFolder);
        ||!FileUtil.isWritableNormalOrSaf(currentFolder)*/
                return;
            }
        }
        else if (requestCode == Constants.REQ_UNICORN_FILE && resultCode == RESULT_OK) {
            ArrayList<String> files = data.getStringArrayListExtra("filePaths");
            String path = files.get(0);
            SharedPref.write(SharedPref.KEY_DESTINATION_FOLDER,path);
            tvFolderPath.setText(path);
        }
    }

    private ArrayList<String> getFilesReadyToShare(){
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
        App.showToast("Share to Light room is turned off temporarily");
        /*File destinationFolder = new File(SharedPref.read(SharedPref.KEY_DESTINATION_FOLDER,SharedPref.DEFAULT_DESTINATION_FOLDER));
        Log.d("abc",destinationFolder.getAbsolutePath());
        if(destinationFolder.exists()){
            // File destinationFolder = new File(tvFolderPath.getText().toString());
            Log.d("abc",destinationFolder.getAbsolutePath());
            ArrayList<String> syncedFiles = getFilesReadyToShare();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putExtra(Intent.EXTRA_SUBJECT, "CR3 files.");
            intent.setType("image/jpeg"); *//* This example is sharing jpeg images. *//*
            intent.setPackage("com.adobe.lrmobile");

            ArrayList<Uri> filesUris = new ArrayList<Uri>();

            for(String path : syncedFiles *//* List of the files you want to send *//*) {
              File file = new File(path);
                if(file.exists()){
                    Uri uri = FileProvider.getUriForFile(this, "com.example.filesynchor.provider",file);
                    filesUris.add(uri);
                }
            }
            Log.d("abc","no of files "+filesUris.size()+"");

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.adobe.lrmobile");
            //startActivity( launchIntent );

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesUris);
            // SharedPref.write(SharedPref.KEY_LAST_SYNC_FILE_PATHS,"");
           // startActivity(intent);
            startActivities(new Intent[]{launchIntent,intent});

            //startActivity(Intent.createChooser(intent, "Share the Pictures"));
        }
        else {
            Toast.makeText(this,"Destination Folder doesn't exist",Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInitialGUi();
        loadSharedPreferenceData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localBroadCast);
    }

    public void onClick(View view) {
        bottomSheetDialog.dismiss();
    }

    private void selectTargetFolder(){
        if(isStoragePermissionGranted()){
            if(StorageDirectory.isSDCardAvailable()){
                final Dialog dialog = new Dialog(this,R.style.Theme_Dialog);
                dialog.setContentView(R.layout.choose_directory_dialog);
                Button btnPhoneStorage = (Button) dialog.findViewById(R.id.btn_phone_storage);
                Button btnSdCard = (Button) dialog.findViewById(R.id.btn_sd_card);
                btnPhoneStorage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchFolderSelectionActivity(StorageDirectory.getInternalStoragePath());
                        dialog.dismiss();
                    }
                });
                btnSdCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File folder = new File (StorageDirectory.getSDCardPath());
                        if (!FileUtil.isWritableNormalOrSaf(folder, mainActivity)) {
                            guideDialogForLEXA(folder.getAbsolutePath());
                        }
                        else {
                            launchFolderSelectionActivity(StorageDirectory.getSDCardPath());
                        }
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
            else {
                launchFolderSelectionActivity(StorageDirectory.getInternalStoragePath());
            }
        }
    }

    private void launchFolderSelectionActivity(String initialDirectory){
        if(isStoragePermissionGranted()){
           /* final Intent chooserIntent = new Intent(ActivityController.this, DirectoryChooserActivity.class);
            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .newDirectoryName("DirChooserSample")
                    .allowReadOnlyDirectory(true)
                    .allowNewDirectoryNameModification(true)
                    .initialDirectory(initialDirectory)
                    .build();
            chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
            startActivityForResult(chooserIntent, REQUEST_DIRECTORY);*/

            UnicornFilePicker.from(ActivityController.this)
                    .addConfigBuilder()
                    .selectMultipleFiles(false)
                    .showOnlyDirectory(true)
                    .setRootDirectory(initialDirectory)
                    .showHiddenFiles(false)
                    .addItemDivider(true)
                    .theme(R.style.UnicornFilePicker_Default)
                    .build()
                    .forResult(Constants.REQ_UNICORN_FILE);
        }
    }

    public void guideDialogForLEXA(String path) {
        final MaterialDialog.Builder x = new MaterialDialog.Builder(mainActivity);
        x.theme(Theme.LIGHT);
        x.title(R.string.needs_access);
        LayoutInflater layoutInflater =
                (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.lexadrawer, null);
        x.customView(view, true);
        // textView
        TextView textView = view.findViewById(R.id.description);
        textView.setText(
                mainActivity.getString(R.string.needs_access_summary)
                        + path
                        + mainActivity.getString(R.string.needs_access_summary1));
        ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_sd_card_48);
        x.positiveText(R.string.open)
                .negativeText(R.string.cancel)
                .positiveColor(getColor(R.color.bright_blue))
                .negativeColor(getColor(R.color.bright_blue))
                .onPositive((dialog, which) -> triggerStorageAccessFramework())
                .onNegative(
                        (dialog, which) ->
                                Toast.makeText(mainActivity, R.string.error, Toast.LENGTH_SHORT).show());
        final MaterialDialog y = x.build();
        y.show();
    }

    private void triggerStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 3);
    }

    private void showAllStorage(){
        List<StorageDirectory> directoryParcelableList = StorageDirectory.getStorageDirectoriesNew();
        for(StorageDirectory directory:directoryParcelableList){
            Log.d("abc",directory.toString());
        }
       /* File targetFile = new File(StorageDirectory.getSDCardPath()+"/story1.txt");
        File sourceFile = new File(StorageDirectory.getInternalStoragePath()+"/story1.txt");
        boolean result = FileUtil.copyFile(sourceFile,targetFile);

        if(result){
            App.showToast("Successfully Copied");
            App.showLog("length: "+targetFile.length());
        }
        else {
            App.showToast("Failed to copy");
        }*/


    }





}
