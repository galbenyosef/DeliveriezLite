package com.gal.deliveriez;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.gal.deliveriez.Utilities.MESSAGE_ACTIVITY_OFFLINE;
import static com.gal.deliveriez.Utilities.MESSAGE_ACTIVITY_ONLINE;
import static com.gal.deliveriez.Utilities.MESSAGE_SHUTDOWN;

public class FragmentDeliverer extends Fragment implements Handler.Callback {

    private ServiceConnection serviceConnection;
    private Messenger mOutgoing,mIncoming;
    private boolean isServiceBound;

    public FragmentDeliverer() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIncoming = new Messenger(new Handler(this));
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                mOutgoing = new Messenger(service);
                try {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().findViewById(R.id.start_service_button).setVisibility(View.GONE);
                            getActivity().findViewById(R.id.stop_service_button).setVisibility(View.VISIBLE);
                            ((TextView)getActivity().findViewById(R.id.status_text)).setText("Service is on");
                        }
                    });
                    isServiceBound=true;
                    Message msg = new Message();
                    msg.what = MESSAGE_ACTIVITY_ONLINE;
                    msg.replyTo = mIncoming;
                    mOutgoing.send(msg);
                }
                catch (RemoteException e) {
                    // In this case the service has crashed before we could even do anything with it
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }

        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deliverer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //activity is singleInstance anyway
        ((MainActivity)getActivity()).checkLocationPermission();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().bindService(new Intent(getActivity(), ServiceDeliverer.class), serviceConnection, Context.BIND_AUTO_CREATE);
        getActivity().findViewById(R.id.start_service_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().bindService(new Intent(getActivity(), ServiceDeliverer.class), serviceConnection, Context.BIND_AUTO_CREATE);
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
        if (isServiceBound){
            Message message = new Message();
            message.what = MESSAGE_ACTIVITY_OFFLINE;
            sendToService(message);
            getActivity().unbindService(serviceConnection);
        }
        super.onPause();
    }

    @Override
    public boolean handleMessage(Message message) {
        return false;

    }


    private void sendToService(Message message){
        try {
            mOutgoing.send(message);
        }
        catch (RemoteException e){
            e.getMessage();
        }
    }

    private void stopService(){
        Intent intent = new Intent(getContext(), ServiceDeliverer.class);
        Message message = new Message();
        message.what = MESSAGE_SHUTDOWN;
        sendToService(message);
        getActivity().unbindService(serviceConnection);
        getActivity().stopService(intent);
        isServiceBound=false;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.start_service_button).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.stop_service_button).setVisibility(View.GONE);
                ((TextView)getActivity().findViewById(R.id.status_text)).setText("Service is off");
            }
        });
        mOutgoing = null;
    }
}
