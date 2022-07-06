package android.example.coba;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.example.coba.BroadCastReceiverServices.BroadCastService;
import android.example.coba.DatabaseHelpers.ProximityDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button proximityPage;
    SensorManager sensorManager;
    Sensor proximitySensor;
    SensorEventListener proximityListener;
    private ProximityDatabaseHelper proximityDatabaseHelper;
    long timeLeftInMilliseconds = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, BroadCastService.class));

        proximityPage = findViewById(R.id.proximityPageId);
        proximityPage.setOnClickListener(this);

        proximityDatabaseHelper = new ProximityDatabaseHelper(this);

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update GUI
            updateGUI(intent);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        onSensorChangedMethod();
        registerReceiver(broadcastReceiver, new IntentFilter(BroadCastService.COUNTDOWN_BR));
//        startTimer();
        // set registerListener for each 4 sensors in sensorManager
        sensorManager.registerListener(proximityListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        // set unregisterListener for each 4 sensors when app is paused
        sensorManager.unregisterListener(proximityListener);
    }

    @Override
    public void onStop() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Sensors detect", Toast.LENGTH_SHORT).show();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, BroadCastService.class));
        super.onDestroy();
    }

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            long millisUntilFinished = intent.getLongExtra("countdown", 30000);
            int seconds = (int) (millisUntilFinished / 1000);

            if(seconds<0){
                long temp4 = proximityDatabaseHelper.countRows();
                for(long i=1; i<=temp4; i++){
                    proximityDatabaseHelper.deleteData(String.valueOf(i));
                }
            }
        }
    }

    // store values of 4 sensors in SQLite database
    public void onSensorChangedMethod() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a");
        String timeData = simpleDateFormat.format(calendar.getTime());


        // Proximity sensor detection code >>>>>>>>>>
        proximityListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
                    // getMaximumRange() is the maximum range of proximity sensor of device
                    // Show result in "centimetre" (cm) unit
                    if(event.values[0] < proximitySensor.getMaximumRange()){
                        // If an object is detected within maximum range of device
                        proximityDatabaseHelper.insertData(timeData, String.valueOf(event.values[0]));
                    } else {
                        // If no object is detected within the maximum range of device
                        proximityDatabaseHelper.insertData(timeData, String.valueOf(event.values[0]));
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.proximityPageId){
            Intent intent = new Intent(getApplicationContext(), ProximityActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder;
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("EXIT !");
        alertDialogBuilder.setMessage("Are you sure you want to close this app ?");
        alertDialogBuilder.setIcon(R.drawable.exit);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });

        alertDialogBuilder.setNeutralButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}