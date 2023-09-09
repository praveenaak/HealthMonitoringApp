package com.example.healthapp;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = "DatabaseHelper";
    static final String DATABASE_NAME = "HEALTHDATA.DB";
    static final int DATABASE_VERSION = 6;
    static final String DATABASE_TABLE = "SYMPTOMS";

    // Columns for the symptoms
    static final String NAUSEA = "NAUSEA";
    static final String HEADACHE = "HEADACHE";
    static final String DIARRHEA = "DIARRHEA";
    static final String SORE_THROAT = "SORE_THROAT";
    static final String FEVER = "FEVER";
    static final String MUSCLE_ACHE = "MUSCLE_ACHE";
    static final String LOSS_OF_SMELL_OR_TASTE = "LOSS_OF_SMELL_OR_TASTE";
    static final String COUGH = "COUGH";
    static final String SHORTNESS_OF_BREATH = "SHORTNESS_OF_BREATH";
    static final String FEELING_TIRED = "FEELING_TIRED";
    static final String HEARTRATE = "HEARTRATE";
    static final String RESPRATE = "RESPRATE";

    private static final String CREATE_DB_QUERY =
            "CREATE TABLE " + DATABASE_TABLE + " (" +
                    NAUSEA + " FLOAT DEFAULT 0, " +
                    HEADACHE + " FLOAT DEFAULT 0, " +
                    DIARRHEA + " FLOAT DEFAULT 0, " +
                    SORE_THROAT + " FLOAT DEFAULT 0, " +
                    FEVER + " FLOAT DEFAULT 0, " +
                    MUSCLE_ACHE + " FLOAT DEFAULT 0, " +
                    LOSS_OF_SMELL_OR_TASTE + " FLOAT DEFAULT 0, " +
                    COUGH + " FLOAT DEFAULT 0, " +
                    SHORTNESS_OF_BREATH + " FLOAT DEFAULT 0, " +
                    FEELING_TIRED + " FLOAT DEFAULT 0, " +
                    HEARTRATE + " STRING , " +
                    RESPRATE + " FLOAT DEFAULT 0);";

    public DatabaseHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB_QUERY);
        Log.i(TAG, "Database created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}
