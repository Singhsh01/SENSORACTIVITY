package com.example.sensoractivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import android.speech.tts.TextToSpeech;

import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;


public class SensorGraphActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor gyroscopeSensor;
    private Sensor proximitySensor;
    private GraphView accelerometerGraph;
    private GraphView gyroscopeGraph;
    private GraphView proximityGraph;
    private TextToSpeech textToSpeech;
    private CountDownTimer countDownTimer;

    private LineGraphSeries<DataPoint> accelerometerSeries;
    private LineGraphSeries<DataPoint> gyroscopeSeries;
    private LineGraphSeries<DataPoint> proximitySeries;
    private boolean isGyroscopeConstant = false;
    private long constantReadingStartTime;
    private boolean isMounted = false;
    private static final double GYROSCOPE_THRESHOLD = 0.08; // Adjust the threshold as needed
    private static final long CONSTANT_READING_DURATION = 11000;// 11 seconds as an example

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the theme here
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_sensor_data_display);

        accelerometerGraph = findViewById(R.id.accelerometerChart);
        gyroscopeGraph = findViewById(R.id.gyroscopeChart);
        proximityGraph = findViewById(R.id.proximityChart);



         accelerometerSeries = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 0),
                new DataPoint(1, 0),
                new DataPoint(2, 0),
                new DataPoint(3, 0),
                new DataPoint(4, 0)
        });

        gyroscopeSeries = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 0),
                new DataPoint(1, 0),
                new DataPoint(2, 0),
                new DataPoint(3, 0),
                new DataPoint(4, 0)
        });

        proximitySeries = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 0),
                new DataPoint(1, 0),
                new DataPoint(2, 0),
                new DataPoint(3, 0),
                new DataPoint(4, 0)
        });




        //Set up chart configurations, data, and labels
        configureGraph(accelerometerGraph, accelerometerSeries, "Accelerometer Data");
        configureGraph(gyroscopeGraph, gyroscopeSeries, "Gyroscope Data");
        configureGraph(proximityGraph, proximitySeries, "Proximity Data");


        // Set manual bounds for the x-axis
        double minXValue = -1.0;
        double maxXValue = 1.0;

        accelerometerGraph.getViewport().setXAxisBoundsManual(true);
        accelerometerGraph.getViewport().setMinX(minXValue);
        accelerometerGraph.getViewport().setMaxX(maxXValue);

        //   accelerometerGraph.getViewport().setMinX(minXValue);  // Replace minXValue with the minimum x-axis value
        //  accelerometerGraph.getViewport().setMaxX(maxXValue);  // Replace maxXValue with the maximum x-axis value

        double minYGyro = -0.005;  // Adjust based on the minimum value from your log
        double maxYGyro = 0.005;
        gyroscopeGraph.getViewport().setXAxisBoundsManual(true);
        gyroscopeGraph.getViewport().setMinX(minYGyro);
        gyroscopeGraph.getViewport().setMaxX(maxYGyro);

        //   // Replace maxXValue with the maximum x-axis value

        double minYProximity = 0.0;    // Adjust based on the minimum value from your log
        double maxYProximity = 10.0;
        proximityGraph.getViewport().setXAxisBoundsManual(true);
        proximityGraph.getViewport().setMinX(minYProximity);
        proximityGraph.getViewport().setMaxX(maxYProximity);  // Replace maxXValue with the maximum x-axis value


        // Enable scaling and zooming for all graphs
        accelerometerGraph.getViewport().setScalable(true);
        accelerometerGraph.getViewport().setScalableY(true);
        gyroscopeGraph.getViewport().setScalable(true);
        gyroscopeGraph.getViewport().setScalableY(true);
        proximityGraph.getViewport().setScalable(true);
        proximityGraph.getViewport().setScalableY(true);

        // Initialize the TextToSpeech engine
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // TextToSpeech engine initialized successfully.
                    // Start the countdown
                    startCountdown();
                }
            }
        });


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }
        private void configureGraph(GraphView graph, LineGraphSeries<DataPoint> series, String label) {
        // Set up graph configurations and label
        graph.addSeries(series);
        graph.setTitle(label);
        // Customize additional graph configurations here.
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScrollableY(true);
    }

    /* Sensor sampling rate of 100 Hz (100 samples per second), the timestamps on
    // the X-axis will increment by 0.01 seconds (10 milliseconds) for each data point.
    // In this case, the X-axis represents time in seconds, and each data point represents the
    // sensor's state at a specific moment in time.*/

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // Log accelerometer data
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.d("SensorData", "Accelerometer Data: X=" + x + ", Y=" + y + ", Z=" + z);

            // Update accelerometer chart
            updateAccelerometerChart(event);

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            // Log gyroscope data
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.d("SensorData", "Gyroscope Data: X=" + x + ", Y=" + y + ", Z=" + z);

            // Check if the gyroscope reading is constant
            boolean isConstant = isGyroscopeConstant(event.values);

            if (isConstant) {
                // Gyroscope reading is constant, start or update the timer
                if (!isGyroscopeConstant) {
                    isGyroscopeConstant = true;
                    constantReadingStartTime = System.currentTimeMillis();
                } else if(!isMounted) {
                    // Check if the constant reading duration has been reached
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - constantReadingStartTime >= CONSTANT_READING_DURATION) {
                        // Change the state to "Mounted" or trigger an action
                        changeStateToMounted();
                    }
                }
            } else {
                // Gyroscope reading has changed, reset the constant reading flag
                isGyroscopeConstant = false;
            }

            // Update gyroscope chart
            updateGyroscopeChart(event);

        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

                float distance = event.values[0];
                Log.d("SensorData", "Proximity Data: Distance=" + distance);
                // Update proximity chart
                updateProximityChart(event);

            // Check if proximity indicates an object nearby
            //  if (distance <= proximitySensor.getMaximumRange()  && !isMounted) {
            //    changeStateToMounted();
            //    }
            }
        }
    private void updateGyroscopeChart(SensorEvent event) {
        // Extract gyroscope data
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Add the data to the gyroscope chart

        gyroscopeSeries.appendData(new DataPoint(System.currentTimeMillis(), x), true, 100);
        // Implement the logic to update the gyroscope chart with this data.
        // For example, add data points to the chart's dataset and refresh the chart.
    }
    private void updateAccelerometerChart(SensorEvent event) {
        // Extract accelerometer data
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Add the data to the accelerometer chart
        accelerometerSeries.appendData(new DataPoint(System.currentTimeMillis(), x), true, 100);
        // Implement the logic to update the accelerometer chart with this data.
        // For example, add data points to the chart's dataset and refresh the chart.
    }

    private void updateProximityChart(SensorEvent event) {
        // Extract proximity data
        float distance = event.values[0];
        // Add the data to the proximity chart
        proximitySeries.appendData(new DataPoint(System.currentTimeMillis(), distance), true, 100);
        // Implement the logic to update the proximity chart with this data.
        // For example, add data points to the chart's dataset and refresh the chart.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
            // Extract proximity data
            float distance = accuracy; // U

        }
    }
    private boolean isGyroscopeConstant(float[] gyroscopeValues) {
        // Compare the gyroscope values to check if they are constant
        double magnitude = Math.sqrt(
                gyroscopeValues[0] * gyroscopeValues[0] +
                        gyroscopeValues[1] * gyroscopeValues[1] +
                        gyroscopeValues[2] * gyroscopeValues[2]
        );
        return magnitude < GYROSCOPE_THRESHOLD;
    }

       private void changeStateToMounted() {
        // Implement the logic to change the state to "Mounted"
           // Show a Toast message
           Toast.makeText(this, "Mounted", Toast.LENGTH_SHORT).show();


           // Update the state
           isMounted = true;


        }
       private void startCountdown() {
           textToSpeech.setSpeechRate(0.9f);
                textToSpeech.speak("Please don't move for 10 seconds", TextToSpeech.QUEUE_FLUSH, null, null);

            new CountDownTimer(12000, 1000) {
                @Override
               public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000);
                    // Count down from 6 to 1
                }
            @Override
            public void onFinish() {
                // Countdown finished, implement the logic to change the state to "Mounted"
                changeStateToMounted();
            }
        }.start();

    }
}