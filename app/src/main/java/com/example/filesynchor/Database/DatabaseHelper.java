package com.example.filesynchor.Database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.filesynchor.App;
import com.example.filesynchor.Data;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int version = 1;
    private static final String database_name = "MYPDC.db";

    public DatabaseHelper() {
        super(App.getContext(),database_name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataRepo.createTable());
        //App.showToast("TABLES ARE CREATED");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ Data.TABLE);
        onCreate(db);

    }



}
