package com.example.healthapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLDataException;

public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DatabaseManager(Context ctx) {
        context = ctx;
    }

    public DatabaseManager open() throws SQLDataException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insertOrUpdate(String column, float rating) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(column, rating);

        if (hasRecords()) {
            // Update the existing record
            database.update(DatabaseHelper.DATABASE_TABLE, contentValue, null, null);
        } else {
            // Insert a new record
            database.insert(DatabaseHelper.DATABASE_TABLE, null, contentValue);
        }
    }
    public void insertOrUpdateRespRate(float respRate) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.RESPRATE, respRate);

        if (hasRecords()) {
            database.update(DatabaseHelper.DATABASE_TABLE, values, null, null);
        } else {
            database.insert(DatabaseHelper.DATABASE_TABLE, null, values);
        }
    }

    public boolean hasRecords() {
        Cursor cursor = database.rawQuery("SELECT 1 FROM " + DatabaseHelper.DATABASE_TABLE + " LIMIT 1", null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
    public Cursor fetchAll() {
        String[] columns = new String[]{
                DatabaseHelper.NAUSEA,
                DatabaseHelper.HEADACHE,
                DatabaseHelper.DIARRHEA,
                DatabaseHelper.SORE_THROAT,
                DatabaseHelper.FEVER,
                DatabaseHelper.MUSCLE_ACHE,
                DatabaseHelper.LOSS_OF_SMELL_OR_TASTE,
                DatabaseHelper.COUGH,
                DatabaseHelper.SHORTNESS_OF_BREATH,
                DatabaseHelper.FEELING_TIRED,
                DatabaseHelper.HEARTRATE,
                DatabaseHelper.RESPRATE
        };
        return database.query(DatabaseHelper.DATABASE_TABLE, columns, null, null, null, null, null);
    }

    public void insertOrUpdateHeartRate(String heartrate) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.HEARTRATE, heartrate);

        if (hasRecords()) {
            database.update(DatabaseHelper.DATABASE_TABLE, values, null, null);
        } else {
            database.insert(DatabaseHelper.DATABASE_TABLE, null, values);
        }
    }
}
