package com.gal.deliveriez;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import static com.gal.deliveriez.Utilities.MESSAGE_ACTIVITY_ONLINE;
import static com.gal.deliveriez.Utilities.MESSAGE_SHUTDOWN;

public class ServiceDeliverer extends Service implements LocationListener, Handler.Callback {

    private Messenger mIncoming,mOutgoing;
    private boolean isActivityRunning;

    private String self_id;
    private Deliverer self;
    private DatabaseReference self_ref;
    private HashMap<String,Integer> notifications;
    private NotificationManagerCompat notificationManager;

    // flag for GPS status
    boolean isGPSEnabled;
    // flag for network status
    boolean isNetworkEnabled;
    // flag for GPS status
    boolean canGetLocation;

    private LocationManager locationManager;
    private Location location; // location

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; // 10 seconds
    // Declaring a Location Manager

    public ServiceDeliverer() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIncoming.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        isGPSEnabled = false;
        isNetworkEnabled = false;
        canGetLocation = false;
        self_id = FirebaseAuth.getInstance().getUid();
        self_ref=FirebaseDatabase.getInstance().getReference().child("deliverers")
                .child(self_id);
        initChannels(getApplicationContext());
        startLocationUpdates();
        startSelfListening();
        mIncoming = new Messenger(new Handler(this));
        notifications = new HashMap<>();
        notificationManager  = NotificationManagerCompat.from(this);
        Log.d("H","service deliverer created");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        Log.d("H","service deliverer started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopUsingGPS();
        super.onDestroy();
    }

    public Location startLocationUpdates() {
        try {
            locationManager = (LocationManager) getApplicationContext()
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    try {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
//                            latitude = location.getLatitude();
//                            longitude = location.getLongitude();
                            }
                        }
                    }
                    catch (SecurityException ex){
                        Log.d("H",ex.getMessage());
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        if (ContextCompat.checkSelfPermission(this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {

                            //Request location updates:
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        }

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                self_ref.child("latitude").setValue(location.getLatitude());
                                self_ref.child("longtitude").setValue(location.getLongitude());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(ServiceDeliverer.this);
        }
    }
    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getApplicationContext().startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        self_ref.child("latitude").setValue(location.getLatitude());
        self_ref.child("longtitude").setValue(location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("H","disabled: "+provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("H","enabled: "+provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("H","changed "+provider+" status "+status);
    }


    private void startSelfListening(){
        self_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    return;
                self = dataSnapshot.getValue(new GenericTypeIndicator<Deliverer>(){});
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_ACTIVITY_ONLINE: {
                isActivityRunning = true;
                mOutgoing = msg.replyTo;
                break;
            }
            case MESSAGE_SHUTDOWN:{
                notificationManager.cancelAll();
            }
            default:
        }
        return true;
    }

}
