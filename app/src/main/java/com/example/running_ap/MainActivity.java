package com.example.running_ap;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
{
    // Constants
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TRACKING_LOCATION_KEY = "tracking_location";
    private static final String TRACKING_LOCATION_LOCATION = "tracking_location";
    private static final String TRACKING_LOCATION_DISTANCE = "tracking_distance";
    private static final String TRACKING_LOCATION_DESIRED_DISTANCE = "tracking_desired_distance";

    // Views
    private Button mLocationButton;
    private TextView mLocationTextView;
    private ImageView mCompass;

    // EditText
    private EditText et_distance;
    private EditText et_hours;
    private EditText et_minutes;
    private EditText et_seconds;

    // Location classes
    private boolean mTrackingLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private Location currentLocation;
    private Location firstLocation;
    private Location previousLocation;

    // Counter
    private CountDownTimer countdowntimer;
    private long startTime;
    private long elapsedTime;

    // Div
    private float distance = 0.0f;
    private float desiredDistance = 0.0f;
    private float finalDistanceReached = 0.0f;
    private boolean bRestart = false;

    private float startRot = 0.0f;
    private float endRot = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationButton = findViewById(R.id.btn_start);
        mLocationTextView = findViewById(R.id.tv_location);
        mCompass = findViewById(R.id.iv_compass);

        // EditText(s)
        et_hours = findViewById(R.id.et_timer_hours);
        et_minutes = findViewById(R.id.et_timer_minuts);
        et_seconds = findViewById(R.id.et_timer_seconds);
        et_distance = findViewById(R.id.et_distance);

        // Initialize the FusedLocationClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Restore the state if the activity is recreated.
        if (savedInstanceState != null)
        {
            mTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);

            mLocationTextView.setText(savedInstanceState.getString(TRACKING_LOCATION_LOCATION));
            mLocationTextView.setVisibility(View.VISIBLE);

            distance = savedInstanceState.getFloat(TRACKING_LOCATION_DISTANCE);
            desiredDistance = savedInstanceState.getFloat(TRACKING_LOCATION_DESIRED_DISTANCE);
        }

        // Set the listener for the location button.
        mLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (et_distance.getText().toString().length() > 0 && (!et_hours.getText().toString().equals("00") || !et_minutes.getText().toString().equals("00") || !et_seconds.getText().toString().equals("00")))
                {
                    if (!mTrackingLocation)
                    {
                        startTrackingLocation();

                        desiredDistance = (et_distance.getText().toString().length() > 0) ? Float.valueOf(et_distance.getText().toString()) : desiredDistance;

                        // Read only
                        et_distance.setEnabled(false);
                        et_seconds.setEnabled(false);
                        et_minutes.setEnabled(false);
                        et_hours.setEnabled(false);

                        startTime = (Long.valueOf(et_hours.getText().toString().matches(".*\\d.*") ? et_hours.getText().toString() : "0") * 60L * 60L) * 1000L;
                        startTime += (Long.valueOf(et_minutes.getText().toString().matches(".*\\d.*") ? et_minutes.getText().toString() : "0") * 60L) * 1000L;
                        startTime += Long.valueOf(et_seconds.getText().toString().matches(".*\\d.*") ? et_seconds.getText().toString() : "0") * 1000L;

                        if (et_seconds.getText().toString().length() == 0)
                            et_seconds.setText(getString(R.string.insertZeroInTimer));
                        else if (Integer.valueOf(et_seconds.getText().toString()) > 59)
                        {
                            et_seconds.setText(getString(R.string.resetTimer_60));
                        }

                        if (et_minutes.getText().toString().length() == 0)
                            et_minutes.setText(getString(R.string.insertZeroInTimer));

                        else if (Integer.valueOf(et_minutes.getText().toString()) > 59)
                        {
                            et_minutes.setText(getString(R.string.resetTimer_60));
                        }

                        if (et_hours.getText().toString().length() == 0)
                            et_hours.setText(getString(R.string.insertZeroInTimer));

                        else if (Integer.valueOf(et_hours.getText().toString()) > 59)
                        {
                            et_hours.setText(getString(R.string.resetTimer_60));
                        }

                        countdowntimer = new CountDownTimer(startTime, 1000)
                        {
                            public void onTick(long millisUntilFinished)
                            {
                                elapsedTime = millisUntilFinished;

                                if (Integer.valueOf(et_seconds.getText().toString()) > 0)
                                {
                                    if (Integer.valueOf(et_seconds.getText().toString()) > 10)
                                    {
                                        et_seconds.setText(String.valueOf(Integer.valueOf(et_seconds.getText().toString()) - 1));
                                    }
                                    else
                                    {
                                        final String s = "0"+String.valueOf(Integer.valueOf(et_seconds.getText().toString()) - 1);
                                        et_seconds.setText(s);
                                    }
                                }
                                else if (Integer.valueOf(et_minutes.getText().toString()) > 0)
                                {
                                    et_seconds.setText(getString(R.string.resetTimer_59));
                                    if (Integer.valueOf(et_minutes.getText().toString()) > 10)
                                    {
                                        et_minutes.setText(String.valueOf(Integer.valueOf(et_minutes.getText().toString()) - 1));
                                    }
                                    else
                                    {
                                        final String s = "0"+String.valueOf(Integer.valueOf(et_minutes.getText().toString()) - 1);
                                        et_minutes.setText(s);
                                    }
                                }
                                else if (Integer.valueOf(et_hours.getText().toString()) > 0)
                                {
                                    et_seconds.setText(getString(R.string.resetTimer_59));
                                    et_minutes.setText(getString(R.string.resetTimer_59));
                                    if (Integer.valueOf(et_hours.getText().toString()) > 10)
                                    {
                                        et_hours.setText(String.valueOf(Integer.valueOf(et_hours.getText().toString()) - 1));
                                    }
                                    else
                                    {
                                        final String s = "0"+String.valueOf(Integer.valueOf(et_hours.getText().toString()) - 1);
                                        et_hours.setText(s);
                                    }
                                }

                                else
                                {
                                    countdowntimer.cancel();
                                    countdowntimer.onFinish();
                                }
                            }
                            public void onFinish()
                            {
                                // You loose
                                stopTrackingLocation();
                                buildNotification(false); // notification
                            }
                        }.start();
                    }
                    else
                    {
                        bRestart = true;
                        stopTrackingLocation();
                        countdowntimer.cancel();
                    }
                }
            }
        });

        // Initialize the location callbacks.
        mLocationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                // If tracking is turned on, reverse geocode into an address
                if (mTrackingLocation)
                {
                    if (mLocationTextView.getVisibility() == View.INVISIBLE)
                    {
                        mLocationTextView.setVisibility(View.VISIBLE);
                    }

                    for (Location location : locationResult.getLocations())
                    {
                        if (currentLocation != null)
                        {
                            previousLocation = currentLocation;
                        }

                        else
                        {
                            firstLocation = location;
                            previousLocation = firstLocation;
                        }

                        currentLocation = location;

                        /* Distance is previous location distance to next location */
                        if (!bRestart)
                        {
                            distance += (previousLocation != null ? (previousLocation.distanceTo(currentLocation) / 1000.0f) : 0.0f);

                            @SuppressLint("DefaultLocale")
                            final String s = String.format("%.3f", Math.max((desiredDistance - distance), 0.0f));
                            et_distance.setText(s);
                        }
                        else
                            bRestart = false;

                        if (distance >= desiredDistance)
                        {
                            // you win
                            countdowntimer.cancel();
                            stopTrackingLocation();

                            finalDistanceReached = distance;
                            distance = 0.0f;

                            buildNotification(true); // notification
                        }

                        mLocationTextView.setText(getString(R.string.location_text, location.getLatitude(), location.getLongitude(), location.getSpeed(), location.getBearing(), location.getAltitude()));

                        /* Get Rotations */
                        startRot = previousLocation.getBearing();
                        endRot = currentLocation.getBearing();

                        RotateAnimation rotate = new RotateAnimation(
                                startRot,
                                endRot,
                                Animation.RELATIVE_TO_SELF,
                                0.5f,
                                Animation.RELATIVE_TO_SELF,
                                0.5f);

                        rotate.setDuration(3000);
                        rotate.setInterpolator(new LinearInterpolator());
                        rotate.setFillAfter(true);
                        rotate.setFillBefore(true);
                        mCompass.setAnimation(rotate);
                    }
                }
            }
        };
    }

    /**
     * Starts tracking the device. Checks for
     * permissions, and requests them if they aren't present. If they are,
     * requests periodic location updates, sets a loading text and starts the
     * animation.
     */
    private void startTrackingLocation()
    {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        else
        {
            mTrackingLocation = true;
            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(),
                            mLocationCallback,
                            null /* Looper */);

            // Set a loading text while you wait for the address to be
            // returned
            mLocationTextView.setText(R.string.location_text);
            mLocationButton.setText(R.string.stop_tracking_location);
        }
    }


    /**
     * Stops tracking the device. Removes the location
     * updates, stops the animation, and resets the UI.
     */
    private void stopTrackingLocation()
    {
        if (mTrackingLocation)
        {
            mTrackingLocation = false;
            mLocationButton.setText(R.string.start);

            et_distance.setEnabled(true);
            et_seconds.setEnabled(true);
            et_minutes.setEnabled(true);
            et_hours.setEnabled(true);
        }
    }


    /**
     * Sets up the location request.
     *
     * @return The LocationRequest object containing the desired parameters.
     */
    private LocationRequest getLocationRequest()
    {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(4500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    /**
     * Saves the last location on configuration change
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        outState.putString(TRACKING_LOCATION_LOCATION, mLocationTextView.getText().toString());
        outState.putFloat(TRACKING_LOCATION_DISTANCE, distance);
        outState.putFloat(TRACKING_LOCATION_DESIRED_DISTANCE, desiredDistance);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_LOCATION_PERMISSION:

                // If the permission is granted, get the location, otherwise,
                // show a Toast
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startTrackingLocation();
                }
                else
                {
                    Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onPause()
    {
        if (mTrackingLocation)
        {
            stopTrackingLocation();
            mTrackingLocation = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        if (mTrackingLocation)
        {
            startTrackingLocation();
        }
        super.onResume();
    }

    private void buildNotification(boolean bCompleted)
    {
        double altitude = (currentLocation.getAltitude() - firstLocation.getAltitude());
        double averageSpeed = ((finalDistanceReached * 1000) / TimeUnit.MILLISECONDS.toSeconds((startTime - elapsedTime)));

        @SuppressLint("DefaultLocale")
        final String s1 = String.format("Distance: %.2f km\nAvg. speed: %.2f m/s\nAltitude difference: %.2f meters above sea level", finalDistanceReached, averageSpeed, altitude);
        @SuppressLint("DefaultLocale")
        final String s2 = String.format("Distance: %.2f km\nAvg. speed: %.2f m/s\nAltitude difference: %.2f meters above sea level", finalDistanceReached, averageSpeed, altitude);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "66");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            builder.setSmallIcon(R.drawable.ic_compass)
                    .setColor(bCompleted ? getResources().getColor(R.color.colorSilentBlue, getTheme()) : getResources().getColor(R.color.colorAccent, getTheme()))
                    .setContentTitle(bCompleted ? "You made it!" : "Time ran out")
                    .setContentText(bCompleted ? s1 : s2)
                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setLights(bCompleted ? Color.GREEN : Color.RED, bCompleted ? 500 : 750, bCompleted ? 500 : 750)
                    .setSound(bCompleted ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        }
        else
        {
            builder.setSmallIcon(R.drawable.ic_compass)
                    .setColor(bCompleted ? getResources().getColor(R.color.colorSilentBlue) : getResources().getColor(R.color.colorAccent))
                    .setContentTitle(bCompleted ? "You made it!" : "Time ran out")
                    .setContentText(bCompleted ? s1 : s2)
                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setLights(bCompleted ? Color.GREEN : Color.RED, bCompleted ? 500 : 750, bCompleted ? 500 : 750)
                    .setSound(bCompleted ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            CharSequence name = "awesome_channel";
            String description = "a very awesome channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("66", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(new Random().nextInt(), builder.build());
    }
}
