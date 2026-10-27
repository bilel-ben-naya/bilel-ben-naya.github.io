package com.example.linkup;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class EventsRepository {

    private final EventsDbHelper helper;

    public EventsRepository(Context context) {
        helper = new EventsDbHelper(context);
    }

    public void insertEvent(String name, String lat, String lng, String type) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(EventsDbHelper.COL_NAME, name);
        values.put(EventsDbHelper.COL_LAT, lat);
        values.put(EventsDbHelper.COL_LNG, lng);
        values.put(EventsDbHelper.COL_TYPE, type);
        db.insert(EventsDbHelper.TABLE_EVENTS, null, values);
        db.close();
    }
}
