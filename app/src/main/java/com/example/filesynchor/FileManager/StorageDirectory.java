/*
 * Copyright (C) 2014-2020 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>,
 * Emmanuel Messulam<emmanuelbendavid@gmail.com>, Raymond Lai <airwave209gt at gmail.com> and Contributors.
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.filesynchor.FileManager;

import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.filesynchor.App;
import com.example.filesynchor.R;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Identifies a mounted volume */
public class StorageDirectory implements Parcelable {
  @NonNull public final String path;
  @NonNull public final String name;
  //public final @DrawableRes int iconRes;
  public final int type;

  public static final int INTERNAL_STORAGE = 1;
  public static final int SDCARD_STORAGE = 2;
  public static final int USB_STORAGE = 3;

  private static final String INTERNAL_SHARED_STORAGE = "Internal shared storage";

  public StorageDirectory(@NonNull String path, @NonNull String name,int type) {
    this.path = path;
    this.name = name;
    //this.iconRes = iconRes;
    this.type = type;
  }

  public StorageDirectory(@NonNull Parcel im) {
    path = im.readString();
    name = im.readString();
    //iconRes = im.readInt();
    type = im.readInt();
  }

  @NonNull
  @Override
  public String toString() {
    String storageType = "UNKNOWN";
    if(type==INTERNAL_STORAGE)
      storageType = "INTERNAL";
    else if(type==SDCARD_STORAGE)
      storageType = "SDCARD";
    else if(type==USB_STORAGE)
      storageType = "USB";
    return "StorageDirectory(path=" + path + ", name=" + name + ", type=" + storageType + ")";
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(path);
    parcel.writeString(name);
    parcel.writeInt(type);
  }

  public static final Creator<StorageDirectory> CREATOR =
      new Creator<StorageDirectory>() {
        public StorageDirectory createFromParcel(Parcel in) {
          return new StorageDirectory(in);
        }

        public StorageDirectory[] newArray(int size) {
          return new StorageDirectory[size];
        }
      };

  //get all the available storage directories
  @RequiresApi(api = Build.VERSION_CODES.N)
  public static synchronized ArrayList<StorageDirectory> getStorageDirectoriesNew() {
    // Final set of paths
    ArrayList<StorageDirectory> volumes = new ArrayList<>();
    StorageManager sm = App.getContext().getSystemService(StorageManager.class);
    for (StorageVolume volume : sm.getStorageVolumes()) {
      if (!volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
              && !volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)) {
        continue;
      }
      File path = getVolumeDirectory(volume);
      String name = volume.getDescription(App.getContext());
      if (INTERNAL_SHARED_STORAGE.equalsIgnoreCase(name)) {
        name = App.getAppContext().getString(R.string.storage_internal);
      }
      int type;
      if (!volume.isRemovable()) {
        type = INTERNAL_STORAGE;
      } else {
        // HACK: There is no reliable way to distinguish USB and SD external storage
        // However it is often enough to check for "USB" String
        if (name.toUpperCase().contains("USB") || path.getPath().toUpperCase().contains("USB")) {
          type = USB_STORAGE;
        } else {
          type = SDCARD_STORAGE;
        }
      }
      volumes.add(new StorageDirectory(path.getPath(), name, type));
    }
    return volumes;
  }

  private static File getVolumeDirectory(StorageVolume volume) {
    try {
      Field f = StorageVolume.class.getDeclaredField("mPath");
      f.setAccessible(true);
      return (File) f.get(volume);
    } catch (Exception e) {
      // This shouldn't fail, as mPath has been there in every version
      throw new RuntimeException(e);
    }
  }

  public static boolean isSDCardAvailable(){
    List<StorageDirectory> directoryList = getStorageDirectoriesNew();
    for(StorageDirectory directory:directoryList){
      if(directory.type==SDCARD_STORAGE)
        return true;
    }
    return false;
  }

  public static boolean isUSBStorageAvailable(){
    List<StorageDirectory> directoryList = getStorageDirectoriesNew();
    for(StorageDirectory directory:directoryList){
      if(directory.type==USB_STORAGE)
        return true;
    }
    return false;
  }

  public static String getSDCardPath(){
    List<StorageDirectory> directoryList = getStorageDirectoriesNew();
    for(StorageDirectory directory:directoryList){
      if(directory.type==SDCARD_STORAGE)
        return directory.path;
    }
    return null;
  }

  public static String getUSBStoragePath(){
   List<StorageDirectory> directoryList = getStorageDirectoriesNew();
    for(StorageDirectory directory:directoryList){
      if(directory.type==USB_STORAGE)
        return directory.path;
    }
    return null;
    //return getInternalStoragePath();
  }

  public static String getInternalStoragePath(){
    return "/storage/emulated/0";
  }


}
