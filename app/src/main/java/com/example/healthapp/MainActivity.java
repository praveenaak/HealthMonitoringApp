package com.example.healthapp;



import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLDataException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button symptomsButton;
    private Button measureHeartRateButton;
    private Button measureRespRateButton;
    private TextView heartRateTextView;
    private TextView respRateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        symptomsButton = (Button) findViewById(R.id.symptoms);
        symptomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSymptomActivity();
            }
        });

        heartRateTextView = findViewById(R.id.textheartrate);

        measureHeartRateButton = findViewById(R.id.measureHeartRate);
        measureHeartRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Play the video
                VideoView videoView = findViewById(R.id.videoView);
                Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.heart);
                videoView.setVideoURI(videoUri);
                videoView.start();

                // Start the heart rate measurement process
                SlowTask task = new SlowTask() {
                    @Override
                    protected void onPostExecute(String result) {
                        super.onPostExecute(result);
                        heartRateTextView.setText(result + " BPM");

                        DatabaseManager dbManager = new DatabaseManager(MainActivity.this);
                        try {
                            dbManager.open();
                        } catch (SQLDataException e) {
                            throw new RuntimeException(e);
                        }
                        dbManager.insertOrUpdateHeartRate(result);
                        dbManager.close();
                    }
                };

                    task.execute(videoUri.toString());

            }
        });

        respRateTextView = findViewById(R.id.textresprate);

        measureRespRateButton = findViewById(R.id.measureRespRate);
        measureRespRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int respRate = callRespiratoryCalculator();
                respRateTextView.setText(String.valueOf(respRate));
                DatabaseManager dbManager = new DatabaseManager(MainActivity.this);
                try {
                    dbManager.open();
                } catch (SQLDataException e) {
                    throw new RuntimeException(e);
                }
                dbManager.insertOrUpdateRespRate(respRate);
                dbManager.close();
            }
        });



    }

    public void openSymptomActivity() {
        Intent intent = new Intent(this, SymptomActivity.class);
        startActivity(intent);
    }

    private int callRespiratoryCalculator() {
        float previousValue = 0f;

        previousValue = 10f;
        int k = 0;
        float[] accelValuesZ = loadCSVData();
        float currentValue = 0f;

        for (int i = 0; i < 3839; i++) {
            currentValue = accelValuesZ[i];
            if (Math.abs(previousValue - currentValue) > 0.15) {
                k++;
            }
            previousValue = currentValue;
        }

        return (int) ((k / 45.0) * 60);
    }

    private float[] loadCSVData() {
        // Load CSV data from the raw directory and return as a float array
        ArrayList<Float> dataList = new ArrayList<>();
        InputStream is = getResources().openRawResource(R.raw.csvbreathe19);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                dataList.add(Float.parseFloat(line.trim()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        float[] dataArray = new float[dataList.size()];
        for (int i = 0; i < dataList.size(); i++) {
            dataArray[i] = dataList.get(i);
        }

        return dataArray;
    }


    private class SlowTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.i("HeartRateApp", "Video path: " + params[0]);

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            ArrayList<Bitmap> frameList = new ArrayList<>();
            try {
                retriever.setDataSource(MainActivity.this, Uri.parse(params[0]));
                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                Log.i("HeartRateApp", "Video duration: " + durationStr);
                int duration = Integer.parseInt(durationStr);
                for (int i = 10; i < duration; i += 1000) {
                    Bitmap bitmap = retriever.getFrameAtTime(i * 1000,MediaMetadataRetriever.OPTION_CLOSEST);
                    frameList.add(bitmap);
                    Log.i("HeartRateApp", "Inside frame retriever" );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("HeartRateApp", "Error setting data source: " + e.getMessage());
            } finally {
                try {
                    Log.i("HeartRateApp", "Inside 2" );
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Log.i("HeartRateApp", "Number of frames extracted: " + frameList.size());


                long redBucket;
                long pixelCount;
                ArrayList<Long> a = new ArrayList<>();
                for (Bitmap bitmap : frameList) {
                    redBucket = 0;
                    pixelCount = 0;
                    for (int y = 10; y < 200; y++) {
                        for (int x = 10; x < 200; x++) {
                            int c = bitmap.getPixel(x, y);
                            pixelCount++;
                            redBucket += Color.red(c) + Color.blue(c) + Color.green(c);
                        }
                    }
                    a.add(redBucket);
                }

                Log.d("HeartRateApp", "Size of list 'a': " + a.size());
                ArrayList<Long> b = new ArrayList<>();
                for (int i = 0; i < a.size() - 5; i++) {
                    long temp = (a.get(i) + a.get(i + 1) + a.get(i + 2) + a.get(i + 3) + a.get(i + 4)) / 4;
                    b.add(temp);
                    Log.d("HeartRateApp", "Adding value to 'b': " + temp);
                }


                if (!b.isEmpty()) {
                    long x = b.get(0);
                    int count = 0;

                    for (int i = 1; i < b.size(); i++) {
                        if (b.get(i) - x > 100) {
                            count++;
                        }
                        x = b.get(i);
                    }

                    int rate = (int) ((count / 45.0) * 60) / 2;
                    return String.valueOf(rate);
                } else {
                    Log.d("HeartRateApp", "List 'b' is empty.");
                    return "Error or default value";
                }

            }
        }
    }
}
