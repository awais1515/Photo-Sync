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

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;


import com.example.filesynchor.App;
import com.example.filesynchor.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;



/** Utility class for helping parsing file systems. */
public abstract class FileUtil {

  private static final String LOG = "AmazeFileUtils";

  private static final Pattern FILENAME_REGEX =
      Pattern.compile("[\\\\\\/:\\*\\?\"<>\\|\\x01-\\x1F\\x7F]", Pattern.CASE_INSENSITIVE);



  public static boolean copyFile(final File source, final File target) {
    Context context = App.getContext();
    FileInputStream inStream = null;
    OutputStream outStream = null;
    FileChannel inChannel = null;
    FileChannel outChannel = null;
    try {
      inStream = new FileInputStream(source);

      // First try the normal way
      if (isWritable(target)) {
        Log.d("abc","Target writeable");
        // standard way
        outStream = new FileOutputStream(target);
        inChannel = inStream.getChannel();
        outChannel = ((FileOutputStream) outStream).getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
      } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          Log.d("abc","AboveLollipop");
          // Storage Access Framework
          DocumentFile targetDocument = getDocumentFile(target, false, context);
          outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
          // Workaround for Kitkat ext SD card
          Uri uri = MediaStoreHack.getUriFromFile(target.getAbsolutePath(), context);
          outStream = context.getContentResolver().openOutputStream(uri);
        } else {
          return false;
        }

        if (outStream != null) {
          // Both for SAF and for Kitkat, write to output stream.
          byte[] buffer = new byte[16384]; // MAGIC_NUMBER
          int bytesRead;
          while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
          }
        }
        else {
          Log.d("abc","outputstream is null");
        }
      }
    } catch (Exception e) {
      Log.d(
              "abc",
              "Error when copying file from "
                      + source.getAbsolutePath()
                      + " to "
                      + target.getAbsolutePath(),
              e);
      return false;
    } finally {
      try {
        inStream.close();
      } catch (Exception e) {
        // ignore exception
      }

      try {
        outStream.close();
      } catch (Exception e) {
        // ignore exception
      }

      try {
        inChannel.close();
      } catch (Exception e) {
        // ignore exception
      }

      try {
        outChannel.close();
      } catch (Exception e) {
        // ignore exception
      }
    }
    return true;
  }


  static boolean deleteFile(@NonNull final File file, Context context) {
    // First try the normal deletion.
    if (file == null) return true;
    boolean fileDelete = rmdir(file, context);
    if (file.delete() || fileDelete) return true;

    // Try with Storage Access Framework.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && isOnExtSdCard(file, context)) {

      DocumentFile document = getDocumentFile(file, false, context);
      return document.delete();
    }

    // Try the Kitkat workaround.
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
      ContentResolver resolver = context.getContentResolver();

      try {
        Uri uri = MediaStoreHack.getUriFromFile(file.getAbsolutePath(), context);
        resolver.delete(uri, null, null);
        return !file.exists();
      } catch (Exception e) {
        Log.e(LOG, "Error when deleting file " + file.getAbsolutePath(), e);
        return false;
      }
    }

    return !file.exists();
  }

  private static boolean rmdir(@NonNull final File file, Context context) {
    if (!file.exists()) return true;

    File[] files = file.listFiles();
    if (files != null && files.length > 0) {
      for (File child : files) {
        rmdir(child, context);
      }
    }

    // Try the normal way
    if (file.delete()) {
      return true;
    }

    // Try with Storage Access Framework.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      DocumentFile document = getDocumentFile(file, true, context);
      if (document != null && document.delete()) {
        return true;
      }
    }

    // Try the Kitkat workaround.
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
      ContentResolver resolver = context.getContentResolver();
      ContentValues values = new ContentValues();
      values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
      resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

      // Delete the created entry, such that content provider will delete the file.
      resolver.delete(
          MediaStore.Files.getContentUri("external"),
          MediaStore.MediaColumns.DATA + "=?",
          new String[] {file.getAbsolutePath()});
    }

    return !file.exists();
  }

  public static boolean isWritable(final File file) {
    if (file == null) return false;
    boolean isExisting = file.exists();

    try {
      FileOutputStream output = new FileOutputStream(file, true);
      try {
        output.close();
      } catch (IOException e) {
        e.printStackTrace();
        // do nothing.
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return false;
    }
    boolean result = file.canWrite();

    // Ensure that file is not created during this process.
    if (!isExisting) {
      file.delete();
    }

    return result;
  }


  public static boolean isWritableNormalOrSaf(final File folder, Context c) {

    // Verify that this is a directory.
    if (folder == null) return false;
    if (!folder.exists() || !folder.isDirectory()) {
      return false;
    }

    // Find a non-existing file in this directory.
    int i = 0;
    File file;
    do {
      String fileName = "AugendiagnoseDummyFile" + (++i);
      file = new File(folder, fileName);
    } while (file.exists());

    // First check regular writability
    if (isWritable(file)) {
      Log.d("abc","Regular write possible");
      return true;
    }
    else {
      Log.d("abc","Regular write not possible");
    }

    // Next check SAF writability.
    DocumentFile document = getDocumentFile(file, false, c);

    if (document == null) {
      return false;
    }

    // This should have created the file - otherwise something is wrong with access URL.
    boolean result = document.canWrite() && file.exists();

    // Ensure that the dummy file is not remaining.
    deleteFile(file, c);
    if(result){
      Log.d("abc","Document write possible");
    }
    else {
      Log.d("abc","Document write not possible");
    }
    return result;
  }

  /**
   * Get a list of external SD card paths. (Kitkat or higher.)
   *
   * @return A list of external SD card paths.
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  private static String[] getExtSdCardPaths(Context context) {
    List<String> paths = new ArrayList<>();
    for (File file : context.getExternalFilesDirs("external")) {
      if (file != null && !file.equals(context.getExternalFilesDir("external"))) {
        int index = file.getAbsolutePath().lastIndexOf("/Android/data");
        if (index < 0) {
          Log.w(LOG, "Unexpected external file dir: " + file.getAbsolutePath());
        } else {
          String path = file.getAbsolutePath().substring(0, index);
          try {
            path = new File(path).getCanonicalPath();
          } catch (IOException e) {
            // Keep non-canonical path.
          }
          paths.add(path);
        }
      }
    }
    if (paths.isEmpty()) paths.add("/storage/sdcard1");
    return paths.toArray(new String[0]);
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static String[] getExtSdCardPathsForActivity(Context context) {
    List<String> paths = new ArrayList<>();
    for (File file : context.getExternalFilesDirs("external")) {
      if (file != null) {
        int index = file.getAbsolutePath().lastIndexOf("/Android/data");
        if (index < 0) {
          Log.w(LOG, "Unexpected external file dir: " + file.getAbsolutePath());
        } else {
          String path = file.getAbsolutePath().substring(0, index);
          try {
            path = new File(path).getCanonicalPath();
          } catch (IOException e) {
            // Keep non-canonical path.
          }
          paths.add(path);
        }
      }
    }
    if (paths.isEmpty()) paths.add("/storage/sdcard1");
    return paths.toArray(new String[0]);
  }

  /**
   * Determine the main folder of the external SD card containing the given file.
   *
   * @param file the file.
   * @return The main folder of the external SD card containing this file, if the file is on an SD
   *     card. Otherwise, null is returned.
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  private static String getExtSdCardFolder(final File file, Context context) {
    String[] extSdPaths = getExtSdCardPaths(context);
    try {
      for (int i = 0; i < extSdPaths.length; i++) {
        if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
          return extSdPaths[i];
        }
      }
    } catch (IOException e) {
      return null;
    }
    return null;
  }

  /**
   * Determine if a file is on external sd card. (Kitkat or higher.)
   *
   * @param file The file.
   * @return true if on external sd card.
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static boolean isOnExtSdCard(final File file, Context c) {
    return getExtSdCardFolder(file, c) != null;
  }

  /**
   * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If
   * the file is not existing, it is created.
   *
   * @param file The file.
   * @param isDirectory flag indicating if the file should be a directory.
   * @return The DocumentFile
   */
  public static DocumentFile getDocumentFile(
      final File file, final boolean isDirectory, Context context) {

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) return DocumentFile.fromFile(file);

    String baseFolder = getExtSdCardFolder(file, context);
    boolean originalDirectory = false;
    if (baseFolder == null) {
      return null;
    }

    String relativePath = null;
    try {
      String fullPath = file.getCanonicalPath();
      if (!baseFolder.equals(fullPath)) relativePath = fullPath.substring(baseFolder.length() + 1);
      else originalDirectory = true;
    } catch (IOException e) {
      return null;
    } catch (Exception f) {
      originalDirectory = true;
      // continue
    }
    String as =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString("URI", null);

    Uri treeUri = null;
    if (as != null) treeUri = Uri.parse(as);
    if (treeUri == null) {
      return null;
    }

    // start with root of SD card and then parse through document tree.
    DocumentFile document = DocumentFile.fromTreeUri(context, treeUri);
    if (originalDirectory) return document;
    String[] parts = relativePath.split("\\/");
    for (int i = 0; i < parts.length; i++) {
      DocumentFile nextDocument = document.findFile(parts[i]);

      if (nextDocument == null) {
        if ((i < parts.length - 1) || isDirectory) {
          nextDocument = document.createDirectory(parts[i]);
        } else {
          nextDocument = document.createFile("image", parts[i]);
        }
      }
      document = nextDocument;
    }

    return document;
  }

  // Utility methods for Kitkat

  /**
   * Copy a resource file into a private target directory, if the target does not yet exist.
   * Required for the Kitkat workaround.
   *
   * @param resource The resource file.
   * @param folderName The folder below app folder where the file is copied to.
   * @param targetName The name of the target file.
   * @return the dummy file.
   */
  private static File copyDummyFile(
      final int resource, final String folderName, final String targetName, Context context)
      throws IOException {
    File externalFilesDir = context.getExternalFilesDir(folderName);
    if (externalFilesDir == null) {
      return null;
    }
    File targetFile = new File(externalFilesDir, targetName);

    if (!targetFile.exists()) {
      InputStream in = null;
      OutputStream out = null;
      try {
        in = context.getResources().openRawResource(resource);
        out = new FileOutputStream(targetFile);
        byte[] buffer = new byte[4096]; // MAGIC_NUMBER
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          out.write(buffer, 0, bytesRead);
        }
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException ex) {
            // do nothing
          }
        }
        if (out != null) {
          try {
            out.close();
          } catch (IOException ex) {
            // do nothing
          }
        }
      }
    }
    return targetFile;
  }

  /**
   * Checks whether the target path exists or is writable
   *
   * @param f the target path
   * @return 1 if exists or writable, 0 if not writable
   */
/*
  public static int checkFolder(final String f, Context context) {
    if (f == null) return 0;
    if (f.startsWith(SMB_URI_PREFIX)
        || f.startsWith(SSH_URI_PREFIX)
        || f.startsWith(OTGUtil.PREFIX_OTG)
        || f.startsWith(CloudHandler.CLOUD_PREFIX_BOX)
        || f.startsWith(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE)
        || f.startsWith(CloudHandler.CLOUD_PREFIX_DROPBOX)
        || f.startsWith(CloudHandler.CLOUD_PREFIX_ONE_DRIVE)) return 1;

    File folder = new File(f);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && isOnExtSdCard(folder, context)) {
      if (!folder.exists() || !folder.isDirectory()) {
        return 0;
      }

      // On Android 5, trigger storage access framework.
      if (isWritableNormalOrSaf(folder, context)) {
        return 1;
      }
    } else if (Build.VERSION.SDK_INT == 19 && isOnExtSdCard(folder, context)) {
      // Assume that Kitkat workaround works
      return 1;
    } else if (folder.canWrite()) {
      return 1;
    } else {
      return 0;
    }
    return 0;
  }
*/

  /**
   * Copy the dummy image and dummy mp3 into the private folder, if not yet there. Required for the
   * Kitkat workaround.
   *
   * @return the dummy mp3.
   */
  private static File copyDummyFiles(Context c) {
    try {
      copyDummyFile(R.mipmap.ic_launcher, "mkdirFiles", "albumart.jpg", c);
      return copyDummyFile(R.raw.temptrack, "mkdirFiles", "temptrack.mp3", c);

    } catch (IOException e) {
      Log.e(LOG, "Could not copy dummy files.", e);
      return null;
    }
  }

  static class MediaFile {
    private static final String NO_MEDIA = ".nomedia";
    private static final String ALBUM_ART_URI = "content://media/external/audio/albumart";
    private static final String[] ALBUM_PROJECTION = {
      BaseColumns._ID, MediaStore.Audio.AlbumColumns.ALBUM_ID, "media_type"
    };

    private static File getExternalFilesDir(Context context) {

      try {
        Method method = Context.class.getMethod("getExternalFilesDir", String.class);
        return (File) method.invoke(context, (String) null);
      } catch (SecurityException ex) {
        //   Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
        return null;
      } catch (NoSuchMethodException ex) {
        //     Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
        return null;
      } catch (IllegalArgumentException ex) {
        // Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
        return null;
      } catch (IllegalAccessException ex) {
        // Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
        return null;
      } catch (InvocationTargetException ex) {
        // Log.d(Maui.LOG_TAG, "Unexpected reflection error.", ex);
        return null;
      }
    }

    private final File file;
    private final Context context;
    private final ContentResolver contentResolver;
    Uri filesUri;

    MediaFile(Context context, File file) {
      this.file = file;
      this.context = context;
      contentResolver = context.getContentResolver();
      filesUri = MediaStore.Files.getContentUri("external");
    }

    /**
     * Deletes the file. Returns true if the file has been successfully deleted or otherwise does
     * not exist. This operation is not recursive.
     */
    public boolean delete() {

      if (!file.exists()) {
        return true;
      }

      boolean directory = file.isDirectory();
      if (directory) {
        // Verify directory does not contain any files/directories within it.
        String[] files = file.list();
        if (files != null && files.length > 0) {
          return false;
        }
      }

      String where = MediaStore.MediaColumns.DATA + "=?";
      String[] selectionArgs = new String[] {file.getAbsolutePath()};

      // Delete the entry from the media database. This will actually delete media files (images,
      // audio, and video).
      contentResolver.delete(filesUri, where, selectionArgs);

      if (file.exists()) {
        // If the file is not a media file, create a new entry suggesting that this location is an
        // image, even
        // though it is not.
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Delete the created entry, such that content provider will delete the file.
        contentResolver.delete(filesUri, where, selectionArgs);
      }

      return !file.exists();
    }

    public File getFile() {
      return file;
    }

    private int getTemporaryAlbumId() {
      final File temporaryTrack;
      try {
        temporaryTrack = installTemporaryTrack();
      } catch (IOException ex) {
        return 0;
      }

      final String[] selectionArgs = {temporaryTrack.getAbsolutePath()};
      Cursor cursor =
          contentResolver.query(
              filesUri, ALBUM_PROJECTION, MediaStore.MediaColumns.DATA + "=?", selectionArgs, null);
      if (cursor == null || !cursor.moveToFirst()) {
        if (cursor != null) {
          cursor.close();
          cursor = null;
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, temporaryTrack.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, "{MediaWrite Workaround}");
        values.put(MediaStore.MediaColumns.SIZE, temporaryTrack.length());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.AudioColumns.IS_MUSIC, true);
        contentResolver.insert(filesUri, values);
      }
      cursor =
          contentResolver.query(
              filesUri, ALBUM_PROJECTION, MediaStore.MediaColumns.DATA + "=?", selectionArgs, null);
      if (cursor == null) {
        return 0;
      }
      if (!cursor.moveToFirst()) {
        cursor.close();
        return 0;
      }
      int id = cursor.getInt(0);
      int albumId = cursor.getInt(1);
      int mediaType = cursor.getInt(2);
      cursor.close();

      ContentValues values = new ContentValues();
      boolean updateRequired = false;
      if (albumId == 0) {
        values.put(MediaStore.Audio.AlbumColumns.ALBUM_ID, 13371337);
        updateRequired = true;
      }
      if (mediaType != 2) {
        values.put("media_type", 2);
        updateRequired = true;
      }
      if (updateRequired) {
        contentResolver.update(filesUri, values, BaseColumns._ID + "=" + id, null);
      }
      cursor =
          contentResolver.query(
              filesUri, ALBUM_PROJECTION, MediaStore.MediaColumns.DATA + "=?", selectionArgs, null);
      if (cursor == null) {
        return 0;
      }

      try {
        if (!cursor.moveToFirst()) {
          return 0;
        }
        return cursor.getInt(1);
      } finally {
        cursor.close();
      }
    }

    private File installTemporaryTrack() throws IOException {
      File externalFilesDir = context.getExternalFilesDir(null);
      if (externalFilesDir == null) {
        return null;
      }
      File temporaryTrack = new File(externalFilesDir, "temptrack.mp3");
      if (!temporaryTrack.exists()) {
        InputStream in = null;
        OutputStream out = null;
        try {
          in = context.getResources().openRawResource(R.raw.temptrack);
          out = new FileOutputStream(temporaryTrack);
          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
          }
        } finally {
          if (in != null) {
            try {
              in.close();
            } catch (IOException ex) {
              return null;
            }
          }
          if (out != null) {
            try {
              out.close();
            } catch (IOException ex) {
              return null;
            }
          }
        }
      }
      return temporaryTrack;
    }

    public boolean mkdir() throws IOException {
      if (file.exists()) {
        return file.isDirectory();
      }

      File tmpFile = new File(file, ".MediaWriteTemp");
      int albumId = getTemporaryAlbumId();

      if (albumId == 0) {
        throw new IOException("Fail");
      }

      Uri albumUri = Uri.parse(ALBUM_ART_URI + '/' + albumId);
      ContentValues values = new ContentValues();
      values.put(MediaStore.MediaColumns.DATA, tmpFile.getAbsolutePath());

      if (contentResolver.update(albumUri, values, null, null) == 0) {
        values.put(MediaStore.Audio.AlbumColumns.ALBUM_ID, albumId);
        contentResolver.insert(Uri.parse(ALBUM_ART_URI), values);
      }

      try {
        ParcelFileDescriptor fd = contentResolver.openFileDescriptor(albumUri, "r");
        fd.close();
      } finally {
        MediaFile tmpMediaFile = new MediaFile(context, tmpFile);
        tmpMediaFile.delete();
      }

      return file.exists();
    }

    /** Returns an OutputStream to write to the file. The file will be truncated immediately. */
    public OutputStream write(long size) throws IOException {

      if (NO_MEDIA.equals(file.getName().trim())) {
        throw new IOException("Unable to create .nomedia file via media content provider API.");
      }

      if (file.exists() && file.isDirectory()) {
        throw new IOException("File exists and is a directory.");
      }

      // Delete any existing entry from the media database.
      // This may also delete the file (for media types), but that is irrelevant as it will be
      // truncated momentarily in any case.
      String where = MediaStore.MediaColumns.DATA + "=?";
      String[] selectionArgs = new String[] {file.getAbsolutePath()};
      contentResolver.delete(filesUri, where, selectionArgs);

      ContentValues values = new ContentValues();
      values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
      values.put(MediaStore.MediaColumns.SIZE, size);
      Uri uri = contentResolver.insert(filesUri, values);

      if (uri == null) {
        // Should not occur.
        throw new IOException("Internal error.");
      }

      return contentResolver.openOutputStream(uri);
    }
  }

  /**
   * Validate given text is a valid filename.
   *
   * @param text
   * @return true if given text is a valid filename
   */

}
