package com.example.linkup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EventsDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "events.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_EVENTS = "TABLE_EVENTS";
    public static final String COL_ID = "ID";
    public static final String COL_NAME = "NAME";
    public static final String COL_LAT = "LAT";
    public static final String COL_LNG = "LNG";
    public static final String COL_TYPE = "TYPE";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_EVENTS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NAME + " TEXT, " +
                    COL_LAT + " TEXT, " +
                    COL_LNG + " TEXT, " +
                    COL_TYPE + " TEXT);";

    public EventsDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }
}
