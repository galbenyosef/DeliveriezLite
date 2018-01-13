package com.gal.deliveriez;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentLive extends Fragment {

    private MapView mapView;
    private GoogleMap googleMap;
    private HashMap<String,Marker> markers;

    private BroadcastReceiver mBroadcastReceiver;
    private IntentFilter intentFilter;

    private TextView delivery_info;
    private Button delivery_info_button;

    public FragmentLive() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentFilter = new IntentFilter();
        intentFilter.addAction("population");
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        markers = new HashMap<>();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_live, container, false);
        mapView =  v.findViewById(R.id.mapView);
        delivery_info = v.findViewById(R.id.delivery_info);
        delivery_info_button = v.findViewById(R.id.delivery_info_button);
        delivery_info.setVisibility(View.GONE);
        delivery_info_button.setVisibility(View.GONE);
        mapView.onCreate(savedInstanceState);
      //  mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int height = displaymetrics.heightPixels;
                mMap.setPadding(0,0,0,height/10);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(false);
                mMap.setMinZoomPreference(12);
                mMap.setMaxZoomPreference(20);
                LatLngBounds bounds = new LatLngBounds(new LatLng(32.783526, 35.041627),new LatLng(32.885175, 35.146499));
                mMap.setLatLngBoundsForCameraTarget(bounds);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(32.833568, 35.069809),5));

            }
       });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context,final Intent intent) {
                final String action = intent.getAction();
                switch (action) {
                    case "population":
                        Log.d("HAHA","default");
                        populate(intent);
                        break;
                    case (Intent.ACTION_CLOSE_SYSTEM_DIALOGS):
                        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                        break;
                    default:
                        Log.d("H","default");
                        break;
                }
            }
        };
        return v;
    }

    private void populate(final Intent intent){
        Log.d("HAHA","goku");

        googleMap.clear();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = intent.getExtras();
                HashMap<String, Deliverer> deliverers = new HashMap<>();
                deliverers = deliverers.getClass().cast(bundle.getSerializable("deliverers"));
                if (deliverers != null) {
                    for (String key : deliverers.keySet()) {
                        Deliverer next = deliverers.get(key);
                        LatLng coord = new LatLng(next.getLatitude(), next.getLongtitude());
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(coord)
                                .title(next.getName())
                                .snippet(next.getPhoneNumber())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        markers.put(key, googleMap.addMarker(markerOptions));
                        Log.d("HAHA",key);
                    }
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver,intentFilter);
        if (mapView !=null)
            mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        if (mapView !=null)
            mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView !=null)
            mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView !=null)
            mapView.onLowMemory();
    }

}
