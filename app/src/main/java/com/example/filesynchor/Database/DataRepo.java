package com.example.filesynchor.Database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.filesynchor.Data;

import java.util.ArrayList;
import java.util.List;

public class DataRepo {


    public static String createTable(){
        return "CREATE TABLE " + Data.TABLE  + "("
                + Data.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT  ,"
                + Data.KEY_SYNCED_FILES  + " INTEGER ,"
                + Data.KEY_SYNCED_GB + " TEXT, "
                + Data.KEY_SYNC_TIME + " TEXT, "
                + Data.KEY_STATUS + " TEXT , "
                + Data.KEY_PATHS  + " TEXT )";
    }
    public static  void insertData(Data data){
        SQLiteDatabase db= DatabaseManager.getInstance().openDatabase();
        db.insert(Data.TABLE,null, getDataContentValues(data));
        DatabaseManager.getInstance().closeDatabase();

    }
    private  static ContentValues getDataContentValues(Data data){
        ContentValues contentValues=new ContentValues();
       // contentValues.put(Data.KEY_ID,data.getId());
        contentValues.put(Data.KEY_SYNC_TIME,data.getSyncTime());
        contentValues.put(Data.KEY_SYNCED_FILES,data.getSyncedFiles());
        contentValues.put(Data.KEY_SYNCED_GB,data.getSyncedGB());
        contentValues.put(Data.KEY_STATUS,data.getStatus());
        contentValues.put(Data.KEY_PATHS,data.getPaths());
        return contentValues;

    }
    public  static  void deleteData(Data data){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        String query =
                "DELETE FROM "+ Data.TABLE+
                        " WHERE "+ Data.KEY_ID +" = "+data.getId();
        db.execSQL(query);
        DatabaseManager.getInstance().closeDatabase();
    }
    public  static  void deleteAllData(){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        String query =
                "DELETE FROM "+ Data.TABLE;
        db.execSQL(query);
        DatabaseManager.getInstance().closeDatabase();
    }

    public static List<Data> getDataList(){
        String query = "SELECT * FROM "+ Data.TABLE +" ORDER BY "+Data.KEY_ID+" DESC";


        List<Data> dataList = new ArrayList<Data>();
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor.moveToFirst()) {
            do
            {
                Data data = new Data();
                data.setId(cursor.getLong(cursor.getColumnIndex(Data.KEY_ID)));
                data.setSyncedFiles(cursor.getInt(cursor.getColumnIndex(Data.KEY_SYNCED_FILES)));
                data.setSyncedGB(cursor.getString(cursor.getColumnIndex(Data.KEY_SYNCED_GB)));
                data.setStatus(cursor.getString(cursor.getColumnIndex(Data.KEY_STATUS)));
                data.setPaths(cursor.getString(cursor.getColumnIndex(Data.KEY_PATHS)));
                data.setSyncTime(cursor.getString(cursor.getColumnIndex(Data.KEY_SYNC_TIME)));
                dataList.add(data);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        DatabaseManager.getInstance().closeDatabase();
        return dataList;
    }
    public static void AddSampleData(){
        Data data1 = new Data("7th August,2020 11:25:28", 10, "5.7 GB", "Completed", "");
        Data data2 = new Data("7th August,2020 09:20:10", 5, "3.7 GB", "Completed", "");
        Data data3 = new Data("6th August,2020 01:35:08", 8, "4.5 GB", "Completed", "");
        Data data4 = new Data("6th August,2020 17:45:46", 10, "5.2 GB", "Completed", "");
        Data data5 = new Data("5th August,2020 23:22:35", 15, "9.1 GB", "Completed", "");
        insertData(data1);
        insertData(data2);
        insertData(data3);
        insertData(data4);
        insertData(data5);
    }
}
