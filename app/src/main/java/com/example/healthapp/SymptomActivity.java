package com.example.healthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;

import java.sql.SQLDataException;
import java.util.HashMap;
import java.util.Map;

public class SymptomActivity extends AppCompatActivity {
    DatabaseManager dbManager;
    private static final String TAG = "SymptomActivity";
    private Spinner symptomSpinner;
    private RatingBar ratingBar;

    // Mapping between spinner values and database column names
    private static final Map<String, String> SYMPTOM_COLUMN_MAPPING = new HashMap<String, String>() {{
        put("Nausea", "NAUSEA");
        put("Headache", "HEADACHE");
        put("Diarrhea", "DIARRHEA");
        put("Sore Throat", "SORE_THROAT");
        put("Fever", "FEVER");
        put("Muscle Ache", "MUSCLE_ACHE");
        put("Loss of Smell or Taste", "LOSS_OF_SMELL_OR_TASTE");
        put("Cough", "COUGH");
        put("Shortness of Breath", "SHORTNESS_OF_BREATH");
        put("Feeling Tired", "FEELING_TIRED");
    }};

    // HashMap to store symptom-rating pairs until "Upload" is pressed
    private final HashMap<String, Float> symptomRatings = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom);

        // Initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        // Setting up the spinner
        symptomSpinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.symptoms_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        symptomSpinner.setAdapter(adapter);

        // Initialize RatingBar
        ratingBar = findViewById(R.id.ratingBar);

        // Set a listener for rating bar changes
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String selectedSymptom = symptomSpinner.getSelectedItem().toString();
                symptomRatings.put(selectedSymptom, rating);  // Store the current symptom-rating pair
            }
        });
    }

    public void UploadPressed(View view) {
        Log.i(TAG,"Inside UploadPressed");
        for (Map.Entry<String, Float> entry : symptomRatings.entrySet()) {
            String selectedSymptom = entry.getKey();
            float symptomRating = entry.getValue();
            String databaseColumn = SYMPTOM_COLUMN_MAPPING.get(selectedSymptom);  // Get the corresponding database column name

            // Insert the symptom and its rating into the database
            dbManager.insertOrUpdate(databaseColumn, symptomRating);
        }
        symptomRatings.clear();

        // Fetch and log all data from the database after upload
        Cursor cursor = dbManager.fetchAll();
        if (cursor.moveToFirst()) {
            do {
                String logStr = "";
                for (String column : cursor.getColumnNames()) {
                    int columnIndex = cursor.getColumnIndex(column);
                    if (columnIndex != -1) {
                        logStr += column + ": " + cursor.getString(columnIndex) + ", ";
                    } else {
                        logStr += column + ": NOT FOUND, ";
                    }
                }
                Log.i(TAG, logStr);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
