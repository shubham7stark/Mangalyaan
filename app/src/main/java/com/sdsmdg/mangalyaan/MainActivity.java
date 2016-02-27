package com.sdsmdg.mangalyaan;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;




import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private SensorManager msensorManager;
    private Sensor msensor, msensorg;
    private long lastupdate = 0;
    TextView xvalue, yvalue, zvalue, xvaluerot, yvaluerot, zvaluerot;
    double[] gravity, linear_acceleration;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    SensorEventListener mAccelerometerSensorListener, mGyroSensorListener;

    double dt;
    int xdisp = 0, ydisp = 0, zdisp = 0;
    int i = 3;
    int counter;
    double acceleration_run_avg[] = new double[3];
    double threshold = 1.0;

    //Game Specific
    Button letsplay;
    Display display;
    Point size = new Point();
    GameSurfaceView gameSurfaceView;
    FrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
        letsplay = (Button)findViewById(R.id.btn_letsplay);
        letsplay.setVisibility(View.GONE);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        frameLayout = (FrameLayout)findViewById(R.id.game_sw);
        gameSurfaceView = new GameSurfaceView(this,size.x,size.y);
        frameLayout.addView(gameSurfaceView);

        lastupdate = System.currentTimeMillis();
        startActivity(new Intent("android.settings.WIFI_SETTINGS"));
        msensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        gravity = new double[3];
        linear_acceleration = new double[3];
        msensor = msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        msensorg = msensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        msensorManager.registerListener(mGyroSensorListener, msensorg, SensorManager.SENSOR_DELAY_NORMAL);
        msensorManager.registerListener(mAccelerometerSensorListener, msensor, SensorManager.SENSOR_DELAY_NORMAL);


        mAccelerometerSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor mysensor = event.sensor;
                Log.e("Insidefirst", "Accelero");
                if (mysensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    long curtime = System.currentTimeMillis();
                    if ((curtime - lastupdate) > 10) {
                        dt = curtime - lastupdate;
                        lastupdate = curtime;
                        final double alpha = 0.8;

                        // Isolate the force of gravity with the low-pass filter.
                        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                        // Remove the gravity contribution with the high-pass filter.
                        linear_acceleration[0] = (event.values[0] - gravity[0]);
                        linear_acceleration[1] = (event.values[1] - gravity[1]);
                        linear_acceleration[2] = (event.values[2] - gravity[2]);

                        if (counter != 5) {
                            acceleration_run_avg[0] += linear_acceleration[0];
                            acceleration_run_avg[1] += linear_acceleration[1];
                            acceleration_run_avg[2] += linear_acceleration[2];
                            counter++;
                        } else {
                            counter = 0;

                            if (acceleration_run_avg[0] > threshold) {
                                xdisp += 1;
                            } else if (acceleration_run_avg[0] < threshold * (-1)) {
                                xdisp -= 1;
                            }

                            ///////////////////////////////////////////////
                            if (acceleration_run_avg[1] > threshold) {
                                ydisp += 1;
                            } else if (acceleration_run_avg[1] < threshold * (-1)) {
                                ydisp -= 1;
                            }
                            ///////////////////////////////////////////////
                            if (acceleration_run_avg[2] > threshold) {
                                zdisp += 1;
                            } else if (acceleration_run_avg[2] < threshold * (-1)) {
                                zdisp -= 1;
                            }
                            ///////////////////////////////////////////////
                            acceleration_run_avg[0] = 0;
                            acceleration_run_avg[1] = 0;
                            acceleration_run_avg[2] = 0;
                        }

                        xvalue.setText(xdisp + " ");
                        yvalue.setText(ydisp + " ");
                        zvalue.setText(zdisp + " ");

                        Integer[] acc_co = new Integer[3];
                        acc_co[0] = xdisp;
                        acc_co[1] = ydisp;
                        acc_co[2] = zdisp;
                    }

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        mGyroSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor mysensor = event.sensor;
                Log.e("InsideSecond", "Gyro");
                if (mysensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    if (timestamp != 0) {
                        final float dT = (event.timestamp - timestamp) * NS2S;
                        // Axis of the rotation sample, not normalized yet.
                        float axisX = event.values[0];
                        float axisY = event.values[1];
                        float axisZ = event.values[2];

                        // Calculate the angular speed of the sample
                        double omegaMagnitude = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                        // Normalize the rotation vector if it's big enough to get the axis
                        // (that is, EPSILON should represent your maximum allowable margin of error)
                        if (omegaMagnitude > Math.pow(10, -2)) {
                            axisX /= omegaMagnitude;
                            axisY /= omegaMagnitude;
                            axisZ /= omegaMagnitude;
                        }

                        // Integrate around this axis with the angular speed by the timestep
                        // in order to get a delta rotation from this sample over the timestep
                        // We will convert this axis-angle representation of the delta rotation
                        // into a quaternion before turning it into the rotation matrix.
                        double thetaOverTwo = omegaMagnitude * dT / 2.0f;
                        double sinThetaOverTwo = Math.sin(thetaOverTwo);
                        double cosThetaOverTwo = Math.cos(thetaOverTwo);
                        deltaRotationVector[0] = (float) (sinThetaOverTwo * axisX);
                        deltaRotationVector[1] = (float) (sinThetaOverTwo * axisY);
                        deltaRotationVector[2] = (float) (sinThetaOverTwo * axisZ);
                        deltaRotationVector[3] = (float) (cosThetaOverTwo);
                        xvaluerot.setText(axisX + "");
                        yvaluerot.setText(axisY + "");
                        zvaluerot.setText(axisZ + "");
                    }
                    timestamp = event.timestamp;
                    float[] deltaRotationMatrix = new float[9];
                    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
                    // User code should concatenate the delta rotation we computed with the current rotation
                    // in order to get the updated rotation.
                    // rotationCurrent = rotationCurrent * deltaRotationMatrix;
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        super.onResume();
        msensorManager.registerListener(mAccelerometerSensorListener, msensor, SensorManager.SENSOR_DELAY_NORMAL);
        msensorManager.registerListener(mGyroSensorListener, msensorg, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        msensorManager.unregisterListener(mAccelerometerSensorListener);
        msensorManager.unregisterListener(mGyroSensorListener);

    }

}