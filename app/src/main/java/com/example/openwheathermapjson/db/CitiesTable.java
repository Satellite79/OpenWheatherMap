package com.example.openwheathermapjson.db;

import android.database.sqlite.SQLiteDatabase;

public class CitiesTable {

    public static final String CITIES_TABLE = "cities";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_CITYID = "city_id";


    private static final String  TABLE_CREATE = "create table " + CITIES_TABLE + "(" + COLUMN_ID  +
                                                 " integer primary key autoincrement, " + COLUMN_NAME +
                                                 " text not null, " + COLUMN_COUNTRY +
                                                 " text not null, " + COLUMN_CITYID +
                                                 " bigint )";

    private static final String TABLE_DROP = "drop table if exists " + CITIES_TABLE;

    public static final String TABLE_INSERT = "insert into " + CITIES_TABLE +
                                              " ( " + COLUMN_NAME + ", "  + COLUMN_COUNTRY + ", " + COLUMN_CITYID + " )  " +
                                               "  values ( ?, ?, ? ) ;" ;

    public static final String TABLE_DELETE = "delete from " + CITIES_TABLE;

    public static void createTable(SQLiteDatabase db){
        db.execSQL(TABLE_CREATE);
        // for example code
        db.execSQL("insert into cities(name, country) values('Moscow','US')");
        db.execSQL("insert into cities(name, country) values('Moscow','RU')");
        db.execSQL("insert into cities(name, country) values('Barcelona','ES')");
        db.execSQL("insert into cities(name, country) values('Berlin','DE')");
        db.execSQL("insert into cities(name, country) values('Saint Petersburg','RU')");
        db.execSQL("insert into cities(name, country) values('Funchal','PT')");
    }

    public static void upgradeTable(SQLiteDatabase db,
                                    int oldVersion,
                                    int newVersion)
    {
        db.execSQL(TABLE_DROP);
        createTable(db);
    }
}
