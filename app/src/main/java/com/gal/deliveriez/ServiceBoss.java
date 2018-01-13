package com.gal.deliveriez;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import static com.gal.deliveriez.Utilities.MESSAGE_ACTIVITY_OFFLINE;
import static com.gal.deliveriez.Utilities.MESSAGE_ACTIVITY_ONLINE;
import static com.gal.deliveriez.Utilities.MESSAGE_DELIVERERS_MARKED;
import static com.gal.deliveriez.Utilities.MESSAGE_GET_DATA;
import static com.gal.deliveriez.Utilities.MESSAGE_SET_DATA;
import static com.gal.deliveriez.Utilities.MESSAGE_HIRE_DELIVERERS;
import static com.gal.deliveriez.Utilities.MESSAGE_SHUTDOWN;

public class ServiceBoss extends Service implements Handler.Callback {

    private HashMap<String,Deliverer> deliverers;

    boolean isActivityRunning = false;
    DatabaseReference deliverers_db;

    boolean deliverersReady;

    private Runnable sendData; //Use to send data to bound activity
    private Handler handler;
    private Messenger mIncoming,mOutgoing;


    public ServiceBoss(){
    }
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        handler = new Handler();
        deliverersReady = false;
        deliverers = new HashMap<>();

        //FireBase References,Listeners
        deliverers_db = FirebaseDatabase.getInstance().getReference("deliverers");

        ChildEventListener deliverers_listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Deliverer deliverer = dataSnapshot.getValue(new GenericTypeIndicator<Deliverer>(){});
                if (deliverer != null) {
                    deliverers.put(dataSnapshot.getKey(), deliverer);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Deliverer deliverer = dataSnapshot.getValue(new GenericTypeIndicator<Deliverer>(){});
                if (deliverer != null) {
                    for (String deliverer_Id : deliverers.keySet()){
                        if (deliverers.get(deliverer_Id).getName().equals(deliverer.getName()) &&
                                deliverers.get(deliverer_Id).getPhoneNumber().equals(deliverer.getPhoneNumber())){
                            deliverers.get(deliverer_Id).setLatitude(deliverer.getLatitude());
                            deliverers.get(deliverer_Id).setLongtitude(deliverer.getLongtitude());
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Deliverer deliverer = dataSnapshot.getValue(new GenericTypeIndicator<Deliverer>(){});
                if (deliverer != null) {
                    for (String deliverer_Id : deliverers.keySet()){
                        if (deliverers.get(deliverer_Id).getName().equals(deliverer.getName()) &&
                                deliverers.get(deliverer_Id).getPhoneNumber().equals(deliverer.getPhoneNumber())){
                            deliverers.remove(deliverer_Id);
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener deliverers_listener_complete = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deliverersReady = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        deliverers_db.addChildEventListener(deliverers_listener);
        deliverers_db.addListenerForSingleValueEvent(deliverers_listener_complete);

        mIncoming = new Messenger(new Handler(this));
        sendData = new Runnable() {
            @Override
            public void run() {
                //After Lists ready, we local broadcasting "sendData"
                if (deliverersReady) {
                    Message msg = new Message();
                    msg.what=MESSAGE_SET_DATA;
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("deliverers",deliverers);
                    msg.setData(bundle);
                    sendMessageToUI(msg);
                }
                else
                    handler.postDelayed(this,1500);
            }
        };
    }
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d("H","service boss started");

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendData);
        super.onDestroy();
    }
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_ACTIVITY_ONLINE:
                isActivityRunning = true;
                mOutgoing = msg.replyTo;
                break;
            case MESSAGE_ACTIVITY_OFFLINE:
                isActivityRunning = false;
                break;
            case MESSAGE_GET_DATA:
                new Handler().post(sendData);
                break;
            default:
        }
        return true;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mIncoming.getBinder();
    }
    private void sendMessageToUI(Message msg) {
        if (mOutgoing==null)
            return;
        try {
            mOutgoing.send(msg);
        }
        catch (RemoteException e) {
            // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
            Log.d("H","The client is dead.");
        }
    }


}
