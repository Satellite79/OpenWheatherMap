package com.example.openwheathermapjson.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class CitiesDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_FILE = "cities.db";
    public static final int DATABASE_VERSION = 1;


    public CitiesDBHelper(Context context)
    {
        super(context, DATABASE_FILE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CitiesTable.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CitiesTable.upgradeTable(db, oldVersion, newVersion);
    }
}
