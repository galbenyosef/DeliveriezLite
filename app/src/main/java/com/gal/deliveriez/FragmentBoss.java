package com.gal.deliveriez;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import static com.gal.deliveriez.Utilities.MESSAGE_ACTIVITY_OFFLINE;
import static com.gal.deliveriez.Utilities.MESSAGE_ACTIVITY_ONLINE;
import static com.gal.deliveriez.Utilities.MESSAGE_DELIVERERS_MARKED;
import static com.gal.deliveriez.Utilities.MESSAGE_GET_DATA;
import static com.gal.deliveriez.Utilities.MESSAGE_SET_DATA;
import static com.gal.deliveriez.Utilities.MESSAGE_HIRE_DELIVERERS;
import static com.gal.deliveriez.Utilities.MESSAGE_SHUTDOWN;

public class FragmentBoss extends Fragment implements Handler.Callback {

    private ServiceConnection serviceConnection;
    private Messenger mOutgoing,mIncoming;
    private boolean isServiceBound;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private ViewPager viewPager;
    private Handler intervalUpdater;
    private Runnable intervalCallback;

    public FragmentBoss() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentFilter = new IntentFilter();
        intervalUpdater = new Handler();
        intentFilter.addAction("pause_updates");
        intentFilter.addAction("resume_updates");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch(action) {
                    case "pause_updates":{
                        intervalUpdater.removeCallbacks(intervalCallback);
                        Log.d("H","PAUSED UPDATES");
                        break;
                    }
                    case "resume_updates":{
                        intervalUpdater.post(intervalCallback);
                        Log.d("H","RESUMED UPDATES");
                        break;
                    }
                }
            }
        };
        mIncoming = new Messenger(new Handler(this));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_boss, container, false);
        viewPager = v.findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        // Add Fragments to adapter one by one
        adapter.addFragment(new FragmentLive(), "LIVE");
        adapter.addFragment(new FragmentList(), "LIST");

        viewPager.setAdapter(adapter);
       // viewPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = v.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager,false);


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver,intentFilter);
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                isServiceBound=true;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().findViewById(R.id.start_service_button).setVisibility(View.GONE);
                        getActivity().findViewById(R.id.stop_service_button).setVisibility(View.VISIBLE);
                    }
                });
                mOutgoing = new Messenger(service);
                Message message = new Message();
                message.replyTo = mIncoming;
                message.what = MESSAGE_ACTIVITY_ONLINE;
                sendToService(message);
                intervalCallback = new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = MESSAGE_GET_DATA;
                        sendToService(message);
                        intervalUpdater.postDelayed(this,10000);
                    }
                };
                intervalUpdater.post(intervalCallback);

            }
            public void onServiceDisconnected(ComponentName className) {

            }
        };
        getActivity().bindService(new Intent(getActivity(), ServiceBoss.class), serviceConnection, Context.BIND_AUTO_CREATE);

        getActivity().findViewById(R.id.start_service_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().bindService(new Intent(getActivity(), ServiceBoss.class), serviceConnection, Context.BIND_AUTO_CREATE);
            }
        });
        getActivity().findViewById(R.id.stop_service_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
            }
        });
        getActivity().findViewById(R.id.sign_out_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isServiceBound)
                    stopService();
                ((MainActivity)getActivity()).signOut();
            }
        });
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        intervalUpdater.removeCallbacks(intervalCallback);
        if (isServiceBound) {
            Message message = new Message();
            message.what = MESSAGE_ACTIVITY_OFFLINE;
            sendToService(message);
            getActivity().unbindService(serviceConnection);
        }
        super.onPause();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case (MESSAGE_SET_DATA):
                Intent intent = new Intent("population");
                intent.putExtras(message.getData());
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                Intent intent2 = new Intent("population_list");
                intent2.putExtras(message.getData());
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent2);

                break;
            default:
        }
        return true;
    }

    private void sendToService(Message message){
        try {
            if (mOutgoing == null)
                return;
            mOutgoing.send(message);
        }
        catch (RemoteException e){
            e.getMessage();
        }
    }

    private void stopService(){
        Intent intent = new Intent(getActivity(), ServiceBoss.class);
        Message message = new Message();
        message.what = MESSAGE_SHUTDOWN;
        sendToService(message);
        getActivity().unbindService(serviceConnection);
        getActivity().stopService(intent);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.start_service_button).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.stop_service_button).setVisibility(View.GONE);
                isServiceBound=false;
            }
        });
        mOutgoing = null;
        Log.d("H","service boss stopped");
    }

}
